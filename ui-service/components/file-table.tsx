"use client"

import { useMemo, useState } from "react"
import useSWR, { mutate } from "swr"
import { swrFetcher, api } from "@/lib/api"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { useToast } from "@/hooks/use-toast"
import { FilePreviewDialog } from "./file-preview-dialog"
import { LuFileSearch } from "react-icons/lu";

type FileItem = {
  id: string
  name: string
  type: string
  size: number
  createdAt: string
  previewUrl?: string
}

function formatBytes(bytes: number) {
  if (bytes === 0) return "0 B"
  const k = 1024
  const sizes = ["B", "KB", "MB", "GB", "TB"]
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${Number.parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

export function FileTable() {
  const { data, isLoading } = useSWR<FileItem[]>("/files/", swrFetcher)
  const [query, setQuery] = useState("")
  const { toast } = useToast()
  const [selected, setSelected] = useState<FileItem | null>(null)


  const filtered = useMemo(() => {
    const q = query.toLowerCase()
    return (data || []).filter(
      (f) =>
        f.name.toLowerCase().includes(q) ||
        f.type.toLowerCase().includes(q) ||
        new Date(f.createdAt).toLocaleString().toLowerCase().includes(q),
    )
  }, [data, query])

  async function onCopy(id: string) {
    try {
      const res = await api("/links/generate", {
        method: "POST",
        body: JSON.stringify({ fileId: id }),
      })
      const url = (res as any)?.url
      await navigator.clipboard.writeText(url)
      toast({ title: "Copied link to clipboard" })
    } catch (e: any) {
      toast({ title: "Copy failed", description: e.message })
    }
  }

  async function onDelete(id: string) {
    try {
      await api(`/files/${id}`, { method: "DELETE" })
      mutate("/files/")
      toast({ title: "File deleted" })
    } catch (e: any) {
      toast({ title: "Delete failed", description: e.message })
    }
  }

  if (isLoading) {
    return (
      <div className="grid place-items-center rounded-2xl border bg-card p-12">
        <p className="text-muted-foreground">Loading files...</p>
      </div>
    )
  }

  if (!data || data.length === 0) {
    return (
      <div className="grid place-items-center rounded-2xl border bg-card p-12 text-center min-h-[75vh]">
        <div className="space-y-2">
          <LuFileSearch className="h-10 w-10 mx-auto text-muted-foreground" />
          <h3 className="text-lg font-semibold">No files yet</h3>
          <p className="text-sm text-muted-foreground">Upload your first file to get started.</p>
        </div>
      </div>
    )
  }

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
                <TableCell onClick={() => setSelected(f)}>{formatBytes(f.size)}</TableCell>
                <TableCell onClick={() => setSelected(f)}>{new Date(f.createdAt).toLocaleString()}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button variant="outline" size="sm" onClick={() => onCopy(f.id)}>
                      Copy Link
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => onDelete(f.id)}>
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
            setSelected(null)
            mutate("/files/")
          }}
        />
      )}
    </div>
  )
}
