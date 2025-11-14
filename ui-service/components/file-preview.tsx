"use client";

import Image from "next/image";

export function FilePreview({ file }: { file: any }) {
  const url = file.previewUrl || file.downloadUrl;

  if (!url) return <div>No preview available</div>;

  if (file.mimeType.startsWith("image/")) {
    return (
      <Image
        src={url}
        alt={file.name}
        width={500}
        height={300}
        className="rounded-lg object-contain max-h-[500px]"
      />
    );
  }

  if (file.mimeType === "application/pdf") {
    return (
      <iframe
        src={url}
        className="w-full h-[500px] rounded-lg"
        title="PDF Preview"
      />
    );
  }

  if (file.mimeType.startsWith("video/")) {
    return (
      <video
        src={url}
        controls
        className="w-full max-h-[500px] rounded-lg"
      />
    );
  }

  if (file.mimeType.startsWith("audio/")) {
    return (
      <audio
        src={url}
        controls
        className="w-full"
      />
    );
  }

  return (
    <div className="text-center text-muted-foreground">
      No preview available<br />
      <a
        href={url}
        className="underline text-primary"
        target="_blank"
        rel="noopener"
      >
        Download
      </a>
    </div>
  );
}
