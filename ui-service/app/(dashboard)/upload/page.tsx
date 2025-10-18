"use client"

import { UploadDropzone } from "@/components/upload-dropzone"

export default function UploadPage() {
  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-pretty text-xl font-semibold">Upload</h2>
        <p className="text-sm text-muted-foreground">Drag & drop files or choose from your device.</p>
      </div>
      <UploadDropzone />
    </div>
  )
}
