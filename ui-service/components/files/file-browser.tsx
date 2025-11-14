"use client";

import useSWR from "swr";
import { swrFetcher } from "@/lib/api";
import FileList from "./file-list";
import FileGrid from "./file-grid";
import { FileItem } from "@/lib/store";

type FileBrowserProps = {
  query?: string;
  sort?: string;
  typeFilter?: string;
  dateFilter?: string;
  view: "grid" | "list";
};

export default function FileBrowser({
  query,
  sort,
  typeFilter = "all",
  dateFilter,
  view,
}: FileBrowserProps) {
  const { data: files, isLoading } = useSWR<FileItem[]>("/files/", swrFetcher);

  if (isLoading) return <p>Loading...</p>;
  if (!files || files.length === 0) return <p>No files found.</p>;

  let filtered: FileItem[] = [...files];

  // 🔍 SEARCH FILTER
  if (query) {
    const q = query.toLowerCase();
    filtered = filtered.filter((f: FileItem) =>
      f.name.toLowerCase().includes(q)
    );
  }

  // 📁 TYPE FILTER
  if (typeFilter !== "all") {
    filtered = filtered.filter((f: FileItem) => f.type.startsWith(typeFilter));
  }

  // 📅 DATE FILTER
  const now = Date.now();
  filtered = filtered.filter((f: FileItem) => {
    const modified = new Date(f.updatedAt || f.createdAt).getTime() ?? 0;

    if (dateFilter === "today") return now - modified < 24 * 60 * 60 * 1000;

    if (dateFilter === "week") return now - modified < 7 * 24 * 60 * 60 * 1000;

    if (dateFilter === "month")
      return now - modified < 30 * 24 * 60 * 60 * 1000;

    return true;
  });

  // 🔄 SORTING
  filtered.sort((a: FileItem, b: FileItem) => {
    if (sort === "name-asc") return a.name.localeCompare(b.name);
    if (sort === "size-desc") return b.size - a.size;
    if (sort === "modified-desc")
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    return 0;
  });

  // 🗂 VIEW SWITCH
  return view === "list" ? (
    <FileList files={filtered} />
  ) : (
    <FileGrid files={filtered} />
  );
}
