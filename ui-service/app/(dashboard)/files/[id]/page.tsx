"use client";

import React, { useState } from "react";
import { notFound, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { format } from "date-fns";
import { FilePreviewDialog } from "@/components/file-preview-dialog";
import { swrFetcher } from "@/lib/api";
import { Loader2, Eye, ArrowLeft } from "lucide-react";
import { FileActions } from "@/components/file-actions";
import { LinkFormDialog } from "@/components/link-form-dialog";
import useSWR from "swr";

type ShareInfo = {
  id: string;
  url: string;
  expiresAt: string | null;
  hasPassword: boolean;
} | null;

type FileItem = {
  id: string;
  name: string;
  type: string;
  size: number;
  createdAt: string;
  updatedAt: string;
  variants: Record<string, string>;
  share: ShareInfo;
};

function formatBytes(bytes: number) {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${Number.parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${
    sizes[i]
  }`;
}

export default function FileDetailPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const { data: file, isLoading, mutate } = useSWR<FileItem>(
    `/files/${params.id}`,
    swrFetcher
  );
  const [previewOpen, setPreviewOpen] = useState(false);
  const [linkDialogOpen, setLinkDialogOpen] = useState(false);
  const [linkFileId, setLinkFileId] = useState<string | null>(null);

  if (isLoading)
    return (
      <div className="flex h-screen items-center justify-center bg-background">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );

  if (!file) return notFound();
  const previewSrc = file.variants?.medium;

  const metadata = [
    { label: "File Name", value: file.name },
    { label: "Content Type", value: file.type },
    { label: "Size", value: formatBytes(file.size) },
    {
      label: "Last Modified",
      value: format(
        new Date(file.updatedAt || file.createdAt),
        "dd MMM yyyy, HH:mm"
      ),
    },
  ];

  return (
    <div className="bg-background h-full">
      <div className="border-b bg-card">
        <div className="flex items-center justify-between px-6 py-4">
          <Button
            variant="ghost"
            onClick={() => router.back()}
            className="gap-2"
          >
            <ArrowLeft className="h-4 w-4" />
            Back
          </Button>
          <h1 className="text-xl font-semibold truncate max-w-md">
            {file.name}
          </h1>
          <div className="w-20" />
        </div>
      </div>

      <div className="flex flex-col lg:flex-row">
        {/* LEFT — MAIN FILE PREVIEW AREA */}
        <div className="flex-1 flex items-center justify-center bg-muted/20">
          <div
            className="relative w-full h-full flex items-center justify-center cursor-pointer group"
            onClick={() => setPreviewOpen(true)}
          >
            <img
              src={previewSrc}
              alt={file.name}
              className="rounded-xl max-w-full max-h-[calc(100vh-200px)] object-contain shadow-2xl transition-transform group-hover:scale-[1.02]"
            />

            <div className="absolute bottom-6 right-6 bg-black/70 text-white text-sm px-4 py-2 rounded-full opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-2">
              <Eye className="h-4 w-4" />
              Click to enlarge
            </div>
          </div>
        </div>

        {/* RIGHT — ACTIONS + INFORMATION */}
        <div className="w-full lg:w-[400px] xl:w-[480px] border-l bg-card p-6 space-y-10 overflow-y-auto">
          <FileActions
            file={{ id: file.id, name: file.name }}
            onPreview={() => setPreviewOpen(true)}
            onShare={() => {
              setLinkFileId(file.id);
              setLinkDialogOpen(true);
            }}
          />

          <div>
            <h2 className="text-lg font-semibold mb-4">File Information</h2>
            <div className="space-y-4">
              {metadata.map((m, idx) => (
                <div
                  key={m.label}
                  className={`flex justify-between text-sm py-3 ${
                    idx === metadata.length - 1 ? "" : "border-b"
                  }`}
                >
                  <span className="text-muted-foreground font-medium">
                    {m.label}
                  </span>
                  <span className="font-semibold text-right max-w-[60%] truncate">
                    {m.value}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
      <FilePreviewDialog
        open={previewOpen}
        onOpenChange={setPreviewOpen}
        file={file}
      />

      {linkFileId && (
        <LinkFormDialog
          open={linkDialogOpen}
          onOpenChange={setLinkDialogOpen}
          fileId={linkFileId}
          existingLink={file.share}
          onLinkDeleted={() => mutate()}
          onLinkGenerated={() => mutate()}
        />
      )}
    </div>
  );
}
