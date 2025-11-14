import { FileItem } from "@/lib/store";
import FileActionsMenu from "./file-menu";
import { formatBytes } from "./file-list";
import { useRouter } from "next/navigation";

type FileGridProps = {
  files: FileItem[];
};

export default function FileGrid({ files }: FileGridProps) {
  const router = useRouter();
  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-5">
      {files.map((f) => (
        <div
          key={f.id}
          className="border rounded-xl p-4 bg-card hover:shadow-md transition cursor-pointer group"
          onClick={() => router.push(`/files/${f.id}`)}
        >
          <img
            src={f.variants.thumbnail}
            alt={f.name}
            className="rounded-md w-full h-32 object-cover"
          />

          <div className="mt-3">
            <p className="font-medium text-sm truncate">{f.name}</p>
            <p className="text-xs text-muted-foreground mt-1">
              {formatBytes(f.size)}
            </p>
          </div>

          <div className="opacity-0 group-hover:opacity-100 transition mt-3 text-right">
            <FileActionsMenu file={f} />
          </div>
        </div>
      ))}
    </div>
  );
}
