"use client";

import { useMemo, useState } from "react";
import useSWR, { mutate } from "swr";
import { swrFetcher, api } from "@/lib/api";
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
import { RiDeleteBin6Fill, RiDownloadLine, RiLinkM } from "react-icons/ri";
import { toast } from "sonner";
import { getToken } from "@/lib/auth";
import { LinkFormDialog } from "./link-form-dialog";
import { useRouter } from "next/navigation";

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

type FileItem = {
  id: string;
  name: string;
  type: string;
  size: number;
  createdAt: string;
  variants: Record<string, string>;
};

function getThumbnailUrl(file: FileItem) {
  return (
    file.variants?.thumbnail ||
    file.variants?.original ||
    "/file-placeholder.jpg"
  );
}

function getMediumUrl(file: FileItem) {
  return (
    file.variants?.medium || file.variants?.original || "/file-placeholder.jpg"
  );
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
  viewMode: "list" | "grid" | "gallery";
  query: string;
};

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
    document.body.appendChild(a);
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

export function FileTable({ viewMode, query }: Readonly<FileTableProps>) {
  const { data, isLoading } = useSWR<FileItem[]>("/files/", swrFetcher);
  const router = useRouter();
  const [linkDialogOpen, setLinkDialogOpen] = useState(false);
  const [linkFileId, setLinkFileId] = useState<string | null>(null);

  const filtered = useMemo(() => {
    const q = query.toLowerCase();
    return (data || []).filter(
      (f) =>
        f.name.toLowerCase().includes(q) ||
        f.type.toLowerCase().includes(q) ||
        new Date(f.createdAt).toLocaleString().toLowerCase().includes(q)
    );
  }, [data, query]);

  const [mainFile, setMainFile] = useState<FileItem | null>(
    filtered[0] || null
  );

  if (isLoading)
    return (
      <div className="grid place-items-center rounded-2xl border bg-card p-12">
        <p className="text-muted-foreground">Loading files...</p>
      </div>
    );
  if (!data || data.length === 0)
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

  // ------------------ LIST VIEW ------------------
  if (viewMode === "list") {
    return (
      <div className="space-y-4">
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
                    <TableCell onClick={() => router.push(`/files/${f.id}`)}>{f.name}</TableCell>
                    <TableCell onClick={() => router.push(`/files/${f.id}`)}>{f.type}</TableCell>
                    <TableCell onClick={() => router.push(`/files/${f.id}`)}>{formatBytes(f.size)}</TableCell>
                    <TableCell onClick={() => router.push(`/files/${f.id}`)}>
                      {new Date(f.createdAt).toLocaleString()}
                    </TableCell>
                  <TableCell className="text-right space-x-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => {
                        setLinkFileId(f.id);
                        setLinkDialogOpen(true);
                      }}
                      className="cursor-pointer"
                    >
                      <RiLinkM />
                    </Button>
                    <Button
                      size="sm"
                      variant="default"
                      onClick={() => onDownload(f.id, f.name)}
                      className="cursor-pointer"
                    >
                      <RiDownloadLine />
                    </Button>
                    <Button
                      size="sm"
                      variant="destructive"
                      onClick={() => onDelete(f.id)}
                      className="cursor-pointer"
                    >
                      <RiDeleteBin6Fill />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
        {linkFileId && (
          <LinkFormDialog
            open={linkDialogOpen}
            onOpenChange={setLinkDialogOpen}
            fileId={linkFileId}
          />
        )}
      </div>
    );
  }

  // ------------------ Grid VIEW ------------------
  if (viewMode === "grid") {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-6">
        {filtered.map((f) => (
          <div
            key={f.id}
            className="flex flex-col items-center justify-center p-4 border rounded-xl shadow-sm cursor-pointer"
            onClick={() => router.push(`/files/${f.id}`)}
          >
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
            <p className="text-xs text-muted-foreground">
              {formatBytes(f.size)}
            </p>
          </div>
        ))}
      </div>
    );
  }

  // ------------------ GALLERY VIEW ------------------
  return (
    <div className="space-y-4">
      {/* Main Preview */}
      {mainFile && (
        <div className="relative border rounded-xl shadow-md overflow-hidden w-full flex justify-center items-center">
          <img
            src={getMediumUrl(mainFile)}
            alt={mainFile.name}
            className="object-contain max-h-[60vh] w-full"
          />
          <div className="absolute top-2 right-2 flex gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => {
                setLinkFileId(mainFile.id);
                setLinkDialogOpen(true);
              }}
            >
              <RiLinkM />
            </Button>
            <Button
              size="sm"
              variant="default"
              onClick={() => onDownload(mainFile.id, mainFile.name)}
            >
              <RiDownloadLine />
            </Button>
            <Button
              size="sm"
              variant="destructive"
              onClick={() => {
                onDelete(mainFile.id);
                setMainFile(null);
              }}
            >
              <RiDeleteBin6Fill />
            </Button>
          </div>
        </div>
      )}

      {/* Thumbnail Strip */}
      <div className="flex gap-3 overflow-x-auto p-2">
        {filtered.map((f) => (
          <div
            key={f.id}
            className={`cursor-pointer border rounded-lg p-1 flex-shrink-0 transition-all duration-200 ${
              mainFile?.id === f.id
                ? "border-blue-500 scale-105"
                : "border-transparent"
            }`}
            onClick={() => setMainFile(f)}
          >
            <img
              src={getThumbnailUrl(f)}
              alt={f.name}
              className="w-20 h-20 object-cover rounded-md"
            />
            <p className="text-xs text-center truncate mt-1 w-20">{f.name}</p>
          </div>
        ))}
      </div>

      {linkFileId && (
        <LinkFormDialog
          open={linkDialogOpen}
          onOpenChange={setLinkDialogOpen}
          fileId={linkFileId}
        />
      )}
    </div>
  );
}
