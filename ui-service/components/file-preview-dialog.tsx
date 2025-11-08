"use client";

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { api } from "@/lib/api";
import { toast } from "sonner";

type Props = {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  file: {
    id: string;
    name: string;
    type: string;
    variants: Record<string, string>;
  };
  onDeleted: () => void;
};

export function FilePreviewDialog({
  open,
  onOpenChange,
  file,
  onDeleted,
}: Props) {
  const [loading, setLoading] = useState(false);

  const isImage = file.type.startsWith("image/");
  const isPdf = file.type === "application/pdf";

  async function onCopyLink() {
    try {
      const data = await api("/links/generate", {
        method: "POST",
        body: JSON.stringify({ fileId: file.id }),
      });
      const url = (data as any)?.url || "";
      await navigator.clipboard.writeText(url);
      toast.success("Copied link to clipboard");
    } catch (e: any) {
      toast.error("Failed to copy", { description: e.message });
    }
  }

  async function onDelete() {
    try {
      setLoading(true);
      await api(`/files/${file.id}`, { method: "DELETE" });
      toast.success("File deleted");
      onOpenChange(false);
      onDeleted();
    } catch (e: any) {
      toast.error("Delete failed", { description: e.message });
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle className="truncate">{file.name}</DialogTitle>
        </DialogHeader>
        <div className="grid gap-4">
          <div className="rounded-xl border bg-muted/40 p-2">
            {isImage && file.variants?.medium ? (
              <img
                src={file.variants?.medium || "/placeholder.svg"}
                alt={file.name}
                className="mx-auto max-h-[60vh] w-auto rounded-lg object-contain"
              />
            ) : isPdf ? (
              <div className="grid place-items-center rounded-lg bg-background p-6">
                <p className="text-sm text-muted-foreground">
                  PDF preview placeholder
                </p>
              </div>
            ) : (
              <div className="grid place-items-center rounded-lg bg-background p-6">
                <p className="text-sm text-muted-foreground">
                  No preview available for this file type
                </p>
              </div>
            )}
          </div>
          <div className="flex gap-2">
            <Button onClick={onCopyLink} variant="default">
              Copy Shareable Link
            </Button>
            <Button onClick={onDelete} variant="destructive" disabled={loading}>
              {loading ? "Deleting..." : "Delete File"}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
