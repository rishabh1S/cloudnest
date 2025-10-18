"use client"

import { FileTable } from "@/components/file-table"

export default function FilesPage() {
  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-pretty text-xl font-semibold">My Files</h2>
        <p className="text-sm text-muted-foreground">Browse, search, preview, and manage your files.</p>
      </div>
      <FileTable />
    </div>
  )
}
