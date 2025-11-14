import { Image, Video, File, FileText, Music, Archive } from "lucide-react";

type Props = {
  mime: string;
  className?: string;
};

export function FileIcon({ mime, className = "h-6 w-6" }: Props) {
  if (mime.startsWith("image/")) return <Image className={className} />;
  if (mime.startsWith("video/")) return <Video className={className} />;
  if (mime.startsWith("audio/")) return <Music className={className} />;
  if (mime.includes("zip") || mime.includes("tar")) return <Archive className={className} />;
  if (mime.includes("pdf") || mime.includes("text")) return <FileText className={className} />;

  return <File className={className} />;
}
