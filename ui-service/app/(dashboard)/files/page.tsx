"use client";

import { useState } from "react";
import { Search, LayoutGrid, List } from "lucide-react";
import { Select, SelectTrigger, SelectContent, SelectItem } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import FileBrowser from "@/components/files/file-browser";

export default function FilesPage() {
  const [view, setView] = useState<"list" | "grid">("list");
  const [query, setQuery] = useState("");
  const [sort, setSort] = useState("modified-desc");
  const [typeFilter, setTypeFilter] = useState("all");
  const [dateFilter, setDateFilter] = useState("any");

  return (
    <div className="space-y-6">
      {/* Top toolbar */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        
        {/* Search Bar */}
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search files..."
            className="pl-10"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>

        {/* Sorting */}
        <Select value={sort} onValueChange={setSort}>
          <SelectTrigger className="w-[180px]">
            Sort by
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="name-asc">Name (A–Z)</SelectItem>
            <SelectItem value="modified-desc">Last Modified (Newest)</SelectItem>
            <SelectItem value="size-desc">Size (Largest)</SelectItem>
          </SelectContent>
        </Select>

        {/* File type filter */}
        <Select value={typeFilter} onValueChange={setTypeFilter}>
          <SelectTrigger className="w-[140px]">Type</SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All</SelectItem>
            <SelectItem value="image">Images</SelectItem>
            <SelectItem value="pdf">Documents</SelectItem>
            <SelectItem value="video">Videos</SelectItem>
            <SelectItem value="audio">Audio</SelectItem>
          </SelectContent>
        </Select>

        {/* Date filter */}
        <Select value={dateFilter} onValueChange={setDateFilter}>
          <SelectTrigger className="w-[160px]">Modified</SelectTrigger>
          <SelectContent>
            <SelectItem value="any">Any time</SelectItem>
            <SelectItem value="today">Today</SelectItem>
            <SelectItem value="week">Last 7 days</SelectItem>
            <SelectItem value="month">Last 30 days</SelectItem>
          </SelectContent>
        </Select>

        {/* View Toggle */}
        <div className="flex gap-2">
          <button
            onClick={() => setView("list")}
            className={`p-2 rounded-md border ${view === "list" ? "bg-muted" : ""}`}
          >
            <List className="h-4 w-4" />
          </button>

          <button
            onClick={() => setView("grid")}
            className={`p-2 rounded-md border ${view === "grid" ? "bg-muted" : ""}`}
          >
            <LayoutGrid className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* File Browser */}
      <FileBrowser
        query={query}
        sort={sort}
        typeFilter={typeFilter}
        dateFilter={dateFilter}
        view={view}
      />
    </div>
  );
}
