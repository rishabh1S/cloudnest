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
};

export function FilePreviewDialog({
  open,
  onOpenChange,
  file,
}: Props) {

  const isImage = file.type.startsWith("image/");
  const isPdf = file.type === "application/pdf";
  const isVideo = file.type.startsWith("video/");

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
            ) : isVideo ? (
              <div className="relative w-full">
                <video
                  controls
                  className="aspect-video overflow-hidden rounded-lg bg-black"
                  poster={file.variants?.medium}
                >
                  <source
                    src={file.variants?.original}
                    type={file.type}
                  />
                  Your browser does not support the video tag.
                </video>
              </div>
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
        </div>
      </DialogContent>
    </Dialog>
  );
}
