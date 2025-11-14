"use client";

import { useRouter } from "next/navigation";
import FileActionsMenu from "./file-menu";
import { format } from "date-fns";
import { FileIcon } from "./file-icon";
import { FileItem } from "@/lib/store";

type FileListProps = {
  files: FileItem[];
};

export const formatBytes = (bytes: number) => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${Number.parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${
    sizes[i]
  }`;
}

export default function FileList({ files }: FileListProps) {
  const router = useRouter();

  return (
    <div className="rounded-xl border bg-card overflow-hidden">
      <table className="w-full text-sm">
        <thead>
          <tr className="bg-muted/40 text-muted-foreground">
            <th className="p-3 text-left font-medium">Name</th>
            <th className="p-3 text-left font-medium">Modified</th>
            <th className="p-3 text-left font-medium">Size</th>
            <th className="p-3 text-right"></th>
          </tr>
        </thead>

        <tbody>
          {files.map((f) => (
            <tr
              key={f.id}
              className="border-t hover:bg-muted/30 transition cursor-pointer"
              onClick={() => router.push(`/files/${f.id}`)}
            >
              <td className="p-3 flex items-center gap-3">
                <FileIcon mime={f.type} />
                <span className="font-medium truncate max-w-[260px]">
                  {f.name}
                </span>
              </td>

              <td className="p-3">
                {format(new Date(f.updatedAt), "dd MMM yyyy")}
              </td>

              <td className="p-3">{formatBytes(f.size)}</td>

              <td className="p-3 text-right">
                <FileActionsMenu file={f} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
