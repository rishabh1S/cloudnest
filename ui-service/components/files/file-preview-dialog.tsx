"use client";

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

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
}: Readonly<Props>) {
  const isImage = file.type.startsWith("image/");
  const isPdf = file.type === "application/pdf";
  const isWord =
    file.type ===
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  const isPpt =
    file.type ===
    "application/vnd.openxmlformats-officedocument.presentationml.presentation";
  const isExcel =
    file.type ===
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  const isText = file.type === "text/plain";
  const isAudio = file.type.startsWith("audio/");
  const isVideo = file.type.startsWith("video/");

  const src = file.variants?.original;
  // Generate Office viewer URL for docs, ppt, excel
  const officeViewerUrl = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
    src || ""
  )}`;

  const renderImagePreview = () => (
    <img
      src={file.variants?.medium || "/placeholder.svg"}
      alt={file.name}
      className="w-full max-h-[70vh] rounded-lg object-contain"
    />
  );

  const renderVideoPreview = () => (
    <div className="relative w-full max-h-[70vh]">
      <video
        controls
        className="aspect-video w-full overflow-hidden rounded-lg bg-black"
        poster={file.variants?.medium}
      >
        <source src={src} type={file.type} />
        Your browser does not support the video tag.
      </video>
    </div>
  );

  const renderAudioPreview = () => (
    <div className="relative w-full max-h-[70vh]">
      <audio src={src} controls className="w-full" />
    </div>
  );

  const renderPdfPreview = () => (
    <iframe
      src={src}
      className="w-full h-[70vh] rounded-lg border-none"
      title={file.name}
    />
  );

  const renderOfficePreview = () => (
    <iframe
      src={officeViewerUrl}
      width="100%"
      height="600px"
      frameBorder="0"
      className="rounded-lg"
      title={file.name}
    >
      This is an embedded{" "}
      <a target="_blank" href="https://office.com" rel="noreferrer">
        Microsoft Office
      </a>{" "}
      document, powered by{" "}
      <a target="_blank" href="https://office.com/webapps" rel="noreferrer">
        Office Online
      </a>
      .
    </iframe>
  );

  const renderTextPreview = () => (
    <iframe
      src={src}
      className="w-full h-[70vh] rounded-lg border-none bg-background p-4 font-mono text-sm"
      title={file.name}
    />
  );

  const renderFallback = () => (
    <div className="grid place-items-center rounded-lg bg-background p-6">
      <p className="text-sm text-muted-foreground">
        No preview available for this file type
      </p>
    </div>
  );

  const renderPreview = () => {
    if (isImage) return renderImagePreview();
    if (isVideo) return renderVideoPreview();
    if (isAudio) return renderAudioPreview();
    if (isPdf) return renderPdfPreview();
    if (isWord || isPpt || isExcel) return renderOfficePreview();
    if (isText) return renderTextPreview();
    return renderFallback();
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-5xl max-h-[90vh] overflow-hidden">
        <DialogHeader>
          <DialogTitle className="truncate">{file.name}</DialogTitle>
        </DialogHeader>

        <div className="rounded-xl border bg-muted/40 p-2">
          {renderPreview()}
        </div>
      </DialogContent>
    </Dialog>
  );
}
