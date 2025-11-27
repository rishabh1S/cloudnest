"use client";

import { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { mutate } from "swr";
import { api } from "@/lib/api";
import { toast } from "sonner";

export function UploadDropzone() {
  const [progress, setProgress] = useState<number>(0);
  const [isUploading, setIsUploading] = useState(false);
  const [link, setLink] = useState<string | null>(null);

  const handleDrop = useCallback(
    async (acceptedFiles: File[]) => {
      if (!acceptedFiles.length) return;
      const file = acceptedFiles[0];
      setIsUploading(true);
      setProgress(0);
      setLink(null);

      try {
        // 1️⃣ Step 1: Request signed upload URL
        const { presignedUrl: uploadUrl, objectKey } = await api("/api/files/upload", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            filename: file.name,
            contentType: file.type,
            size: file.size,
          }),
        });

        // 2️⃣ Step 2: Upload file directly with progress tracking
        await new Promise<void>((resolve, reject) => {
          const xhr = new XMLHttpRequest();
          xhr.open("PUT", uploadUrl, true);
          xhr.setRequestHeader("Content-Type", file.type);

          // Progress event handler
          xhr.upload.onprogress = (event) => {
            if (event.lengthComputable) {
              const percent = (event.loaded / event.total) * 100;
              setProgress(percent);
            }
          };

          xhr.onload = () => {
            if (xhr.status >= 200 && xhr.status < 300) {
              setProgress(100);
              resolve();
            } else {
              reject(new Error(`Upload failed with status ${xhr.status}`));
            }
          };

          xhr.onerror = () => reject(new Error("Network error during upload"));
          xhr.send(file);
        });

        // 3️⃣ Step 3: Notify backend that upload completed
        const data = await api("/api/files/complete", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ objectKey }),
        });

        setLink(data.url);
        mutate("/files");
        toast.success("Upload complete");
      } catch (err: any) {
        toast.error("Upload failed", { description: err.message });
        setProgress(0);
      } finally {
        setIsUploading(false);
      }
    },
    [toast]
  );

  const { getRootProps, getInputProps, isDragActive, open } = useDropzone({
    multiple: false,
    onDrop: (acceptedFiles: File[]) => {
      void handleDrop(acceptedFiles);
    },
    noClick: true,
  });

  return (
    <div className="space-y-6">
      <div
        {...getRootProps()}
        className={`grid place-items-center rounded-2xl border-2 border-dashed p-12 min-h-[75vh] text-center transition-colors ${
          isDragActive ? "bg-muted/60" : "hover:bg-muted/40"
        }`}
        aria-label="Drag and drop upload area"
      >
        <input {...getInputProps()} />
        <div>
          <h3 className="mb-1 text-lg font-semibold">
            {isDragActive ? "Drop the file here..." : "Drag & Drop your file"}
          </h3>
          <p className="text-sm text-muted-foreground">or</p>
          <div className="mt-3">
            <Button type="button" onClick={open} disabled={isUploading}>
              {isUploading ? "Uploading..." : "Choose File"}
            </Button>
          </div>
        </div>
      </div>

      {isUploading && (
        <div className="space-y-2">
          <Progress
            value={progress}
            className="transition-all duration-300 ease-out"
            aria-label="Upload progress"
          />
          <p className="text-sm text-muted-foreground">
            Uploading... {Math.floor(progress)}%
          </p>
        </div>
      )}

      {link && !isUploading && (
        <div className="rounded-xl border bg-card p-4 transition-all duration-300 ease-in-out">
          <p className="text-sm">File uploaded successfully 🎉</p>
          <div className="mt-2 flex items-center gap-2">
            <code className="rounded-md bg-muted px-2 py-1 text-xs break-all">
              {link}
            </code>
            <Button
              size="sm"
              variant="outline"
              onClick={async () => {
                await navigator.clipboard.writeText(link);
                toast("Copied hosted link");
              }}
            >
              Copy Link
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
