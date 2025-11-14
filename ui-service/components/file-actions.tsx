"use client";

import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import { getToken } from "@/lib/auth";
import { Download, Eye, Share2, Trash2 } from "lucide-react";
import React from "react";
import { toast } from "sonner";
import { mutate } from "swr";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";


const API_BASE = process.env.NEXT_PUBLIC_API_URL;

async function onDownload(id: string, name: string) {
  try {
    const token = getToken();
    const res = await fetch(`${API_BASE}/files/download/${id}`, {
      method: "GET",
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });

    if (!res.ok) throw new Error("Download failed");

    const blob = await res.blob();
    const url = globalThis.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = name;
    a.click();
    a.remove();
    globalThis.URL.revokeObjectURL(url);

    toast.success("Download started");
  } catch (e: any) {
    toast.error("Download failed", { description: e.message });
  }
}

async function onDelete(id: string) {
  try {
    await api(`/files/${id}`, { method: "DELETE" });
    mutate("/files/");
    toast.success("File deleted");
  } catch (e: any) {
    toast.error("Delete failed", { description: e.message });
  }
}

export function FileActions({
  file,
  onPreview,
  onShare
}: Readonly<{
  file: { id: string; name: string };
  onPreview: () => void;
  onShare: () => void;
}>) {
  return (
    <div>
      <h2 className="text-lg font-semibold mb-4">Actions</h2>

      <div className="grid grid-cols-1 gap-6">
        {/* Download */}
        <Button
          variant="outline"
          onClick={() => onDownload(file.id, file.name)}
          className="flex items-center gap-2 h-auto py-3"
        >
          <Download className="h-4 w-4" />
          Download
        </Button>

        {/* Preview */}
        <Button
          variant="outline"
          onClick={onPreview}
          className="flex items-center gap-2 h-auto py-3"
        >
          <Eye className="h-4 w-4" />
          Preview
        </Button>

        {/* Share */}
        <Button
          variant="outline"
          onClick={onShare}
          className="flex items-center gap-2 h-auto py-3"
        >
          <Share2 className="h-4 w-4" />
          Share
        </Button>

        {/* Delete */}
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button
              variant="destructive"
              className="flex items-center gap-2 h-auto py-3"
            >
              <Trash2 className="h-4 w-4" />
              Delete
            </Button>
          </AlertDialogTrigger>

          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete File?</AlertDialogTitle>
              <AlertDialogDescription>
                Are you sure you want to delete <strong>{file.name}</strong>?
                <br />
                This action cannot be undone.
              </AlertDialogDescription>
            </AlertDialogHeader>

            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction onClick={() => onDelete(file.id)}>
                Delete
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>
    </div>
  );
}
