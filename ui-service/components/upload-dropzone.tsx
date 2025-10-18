"use client"

import { useCallback, useRef, useState } from "react"
import { useDropzone } from "react-dropzone"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"
import { useToast } from "@/hooks/use-toast"
import { mutate } from "swr"

export function UploadDropzone() {
  const inputRef = useRef<HTMLInputElement | null>(null)
  const [progress, setProgress] = useState<number | null>(null)
  const [link, setLink] = useState<string | null>(null)
  const { toast } = useToast()

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      if (!acceptedFiles.length) return
      const file = acceptedFiles[0]
      setProgress(5)
      setLink(null)
      try {
        const fd = new FormData()
        fd.append("file", file)
        const res = await fetch("/files/upload", {
          method: "POST",
          body: fd,
        })
        setProgress(80)
        if (!res.ok) throw new Error(await res.text())
        const data = (await res.json()) as { fileId: string; url: string }
        setProgress(100)
        setLink(data.url)
        mutate("/files")
        toast({ title: "Upload complete" })
      } catch (e: any) {
        toast({ title: "Upload failed", description: e.message })
        setProgress(null)
      }
    },
    [toast],
  )

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    multiple: false,
    onDrop,
  })

  function chooseFile() {
    inputRef.current?.click()
  }

  return (
    <div className="space-y-4">
      <div
        {...getRootProps()}
        className="grid place-items-center rounded-2xl border-2 border-dashed p-10 text-center transition-colors hover:bg-muted/40"
        aria-label="Drag and drop upload area"
      >
        <input {...getInputProps()} />
        <div>
          <h3 className="mb-1 text-lg font-semibold">
            {isDragActive ? "Drop the file here..." : "Drag & Drop your file"}
          </h3>
          <p className="text-sm text-muted-foreground">or</p>
          <div className="mt-3">
            <Button type="button" variant="default" onClick={chooseFile}>
              Choose File
            </Button>
          </div>
          <input
            ref={inputRef}
            type="file"
            className="hidden"
            onChange={(e) => {
              const f = e.target.files?.[0]
              if (f) onDrop([f])
            }}
          />
        </div>
      </div>

      {progress !== null && (
        <div className="space-y-2">
          <Progress value={progress} aria-label="Upload progress" />
          <p className="text-sm text-muted-foreground">Uploading...</p>
        </div>
      )}

      {link && (
        <div className="rounded-xl border bg-card p-4">
          <p className="text-sm">File uploaded successfully.</p>
          <div className="mt-2 flex items-center gap-2">
            <code className="rounded-md bg-muted px-2 py-1 text-xs">{link}</code>
            <Button
              size="sm"
              variant="outline"
              onClick={async () => {
                await navigator.clipboard.writeText(link)
                toast({ title: "Copied hosted link" })
              }}
            >
              Copy Hosted Link
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
