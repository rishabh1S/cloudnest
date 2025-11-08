"use client";

import { useMemo, useState } from "react";
import useSWR, { mutate } from "swr";
import { swrFetcher, api } from "@/lib/api";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { LuFileSearch } from "react-icons/lu";
import { RiDeleteBin6Fill } from "react-icons/ri";
import { FilePreviewDialog } from "./file-preview-dialog";
import { toast } from "sonner";

type FileItem = {
  id: string;
  name: string;
  type: string;
  size: number;
  createdAt: string;
  variants: Record<string, string>;
};

function getThumbnailUrl(file: FileItem) {
  return file.variants?.thumbnail || file.variants?.original || "/file-placeholder.jpg";
}

function formatBytes(bytes: number) {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${Number.parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${
    sizes[i]
  }`;
}

type FileTableProps = {
  viewMode: "list" | "icon" | "big-icon";
};

async function onCopy(id: string) {
    try {
      const res = await api("/links/generate", {
        method: "POST",
        body: JSON.stringify({ fileId: id }),
      });
      const url = (res as any)?.url;
      await navigator.clipboard.writeText(url);
      toast.success("Copied link to clipboard");
    } catch (e: any) {
      toast.error("Copy failed",{ description: e.message });
    }
  }

  async function onDelete(id: string) {
    try {
      await api(`/files/${id}`, { method: "DELETE" });
      mutate("/files/");
      toast.success("File deleted");
    } catch (e: any) {
      toast.error("Delete failed",{ description: e.message });
    }
  }

export function FileTable({ viewMode }: Readonly<FileTableProps>) {
  const { data, isLoading } = useSWR<FileItem[]>("/files/", swrFetcher);
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState<FileItem | null>(null);

  const filtered = useMemo(() => {
    const q = query.toLowerCase();
    return (data || []).filter(
      (f) =>
        f.name.toLowerCase().includes(q) ||
        f.type.toLowerCase().includes(q) ||
        new Date(f.createdAt).toLocaleString().toLowerCase().includes(q)
    );
  }, [data, query]);

  if (isLoading) {
    return (
      <div className="grid place-items-center rounded-2xl border bg-card p-12">
        <p className="text-muted-foreground">Loading files...</p>
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="grid place-items-center rounded-2xl border bg-card p-12 text-center min-h-[75vh]">
        <div className="space-y-2">
          <LuFileSearch className="h-10 w-10 mx-auto text-muted-foreground" />
          <h3 className="text-lg font-semibold">No files yet</h3>
          <p className="text-sm text-muted-foreground">
            Upload your first file to get started.
          </p>
        </div>
      </div>
    );
  }

  // Render Files Based on Selected View Mode
  if (viewMode === "list") {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between gap-3">
          <Input
            placeholder="Search files..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="max-w-xs"
          />
        </div>
        <div className="overflow-x-auto rounded-2xl border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>File name</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Size</TableHead>
                <TableHead>Created</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.map((f) => (
                <TableRow key={f.id} className="cursor-pointer">
                  <TableCell onClick={() => setSelected(f)}>{f.name}</TableCell>
                  <TableCell onClick={() => setSelected(f)}>{f.type}</TableCell>
                  <TableCell onClick={() => setSelected(f)}>
                    {formatBytes(f.size)}
                  </TableCell>
                  <TableCell onClick={() => setSelected(f)}>
                    {new Date(f.createdAt).toLocaleString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => onCopy(f.id)}
                      >
                        Copy Link
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => onDelete(f.id)}
                      >
                        Delete
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
        {selected && (
          <FilePreviewDialog
            open={!!selected}
            onOpenChange={(v) => !v && setSelected(null)}
            file={selected}
            onDeleted={() => {
              setSelected(null);
              mutate("/files/");
            }}
          />
        )}
      </div>
    );
  }

  // Render for Big Icon View
  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-6">
      {filtered.map((f) => (
        <div
          key={f.id}
          className="relative flex flex-col items-center justify-center p-4 border rounded-xl shadow-sm"
        >
          {/* Show file preview icon or default icon */}
          <div className="w-16 h-16 mb-3">
            <img
              src={getThumbnailUrl(f)}
              alt={f.name}
              className="object-cover w-full h-full rounded-lg"
            />
          </div>
          <p className="text-sm font-medium">
            {f.name.length > 12
              ? `${f.name.slice(0, 12)}...${f.name.slice(
                  f.name.lastIndexOf(".")
                )}`
              : f.name}
          </p>
          <p className="text-xs text-muted-foreground">{formatBytes(f.size)}</p>
          <RiDeleteBin6Fill onClick={() => onDelete(f.id)} 
          className="absolute top-2 right-2 cursor-pointer text-muted-foreground hover:text-red-500" />
        </div>
      ))}
    </div>
  );
}
