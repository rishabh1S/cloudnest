"use client";

import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { MoreVertical, Download, Share2, Trash, Edit } from "lucide-react";
import { onDelete, onDownload } from "./file-actions";
import { useRouter } from "next/navigation";
import { FileItem } from "@/lib/store";

type FileActionsMenuProps = {
  file: FileItem;
};

export default function FileActionsMenu({ file }: FileActionsMenuProps) {
  const router = useRouter();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        className="
    inline-flex h-9 w-9 items-center justify-center 
    rounded-md hover:bg-muted transition-colors
    focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring
  "
        onClick={(e) => e.stopPropagation()}
      >
          <MoreVertical className="h-4 w-4" />
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={() => onDownload(file.id, file.name)}>
          <Download className="mr-2 h-4 w-4" /> Download
        </DropdownMenuItem>

        <DropdownMenuItem
          onClick={() => router.push(`/files/${file.id}?rename=true`)}
        >
          <Edit className="mr-2 h-4 w-4" /> Rename
        </DropdownMenuItem>

        <DropdownMenuItem
          onClick={() => router.push(`/files/${file.id}?share=true`)}
        >
          <Share2 className="mr-2 h-4 w-4" /> Share
        </DropdownMenuItem>

        <DropdownMenuItem
          onClick={() => onDelete(file.id)}
        >
          <Trash className="mr-2 h-4 w-4" /> Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
