"use client";

import { useState } from "react";
import { FileTable } from "@/components/file-table";
import { LuLayoutList, LuLayoutGrid, LuImage } from "react-icons/lu";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";

export default function FilesPage() {
  const [viewMode, setViewMode] = useState<"list" | "icon" | "gallery">(
    "list"
  );
  const [query, setQuery] = useState("");

  const handleViewChange = (view: "list" | "icon" | "gallery") => {
    setViewMode(view);
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <div className="relative hidden md:flex items-center flex-1 max-w-md">
        <Search className="absolute left-3 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search files..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="pl-10 bg-card border-border focus:border-primary"
        />
        </div>

        {/* Select Dropdown for View Mode */}
        <Select onValueChange={handleViewChange} value={viewMode}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Select View Mode" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectLabel>View Options</SelectLabel>
              <SelectItem value="list">
                <div className="flex items-center gap-2">
                  <LuLayoutList className="w-4 h-4" />
                  List View
                </div>
              </SelectItem>
              <SelectItem value="icon">
                <div className="flex items-center gap-2">
                  <LuLayoutGrid className="w-4 h-4" />
                  Icon View
                </div>
              </SelectItem>
              <SelectItem value="big-icon">
                <div className="flex items-center gap-2">
                  <LuImage className="w-4 h-4" />
                  Gallery View
                </div>
              </SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      {/* File Display */}
      <FileTable viewMode={viewMode} query={query} />
    </div>
  );
}
