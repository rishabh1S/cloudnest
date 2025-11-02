"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useDropzone } from "react-dropzone";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { useToast } from "@/hooks/use-toast";
import { mutate } from "swr";
import { api } from "@/lib/api";

export function UploadDropzone() {
  const [progress, setProgress] = useState<number>(0);
  const [isUploading, setIsUploading] = useState(false);
  const [link, setLink] = useState<string | null>(null);
  const { toast } = useToast();

  const animateProgress = (target: number, speed = 5) => {
    // Smoothly increments progress toward target
    setProgress((prev) => {
      if (prev < target) {
        const diff = target - prev;
        return prev + Math.max(1, diff / speed);
      }
      return prev;
    });
  };

  // Smooth progress animation
  useEffect(() => {
    if (!isUploading) return;
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev < 90) return prev + Math.random() * 2; // idle animation
        return prev;
      });
    }, 200);
    return () => clearInterval(interval);
  }, [isUploading]);

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      if (!acceptedFiles.length) return;
      const file = acceptedFiles[0];
      setIsUploading(true);
      setProgress(5);
      setLink(null);

      try {
        const fd = new FormData();
        fd.append("file", file);

        // Upload
        const res = await api("/files/upload", {
          method: "POST",
          body: fd,
        });

        if (!res.ok) throw new Error(await res.text());
        animateProgress(95);

        const data = (await res.json()) as { fileId: string; url: string };

        animateProgress(100);
        setLink(data.url);
        mutate("/files");
        toast({ title: "Upload complete" });

        setTimeout(() => {
          setIsUploading(false);
          setProgress(100);
        }, 500);
      } catch (e: any) {
        toast({ title: "Upload failed", description: e.message });
        setProgress(0);
        setIsUploading(false);
      }
    },
    [toast]
  );

  const { getRootProps, getInputProps, isDragActive, open } = useDropzone({
    multiple: false,
    onDrop,
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
                toast({ title: "Copied hosted link" });
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
