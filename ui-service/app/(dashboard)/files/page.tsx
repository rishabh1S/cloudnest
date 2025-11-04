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

export default function FilesPage() {
  const [viewMode, setViewMode] = useState<"list" | "icon" | "big-icon">(
    "list"
  );

  const handleViewChange = (view: "list" | "icon" | "big-icon") => {
    setViewMode(view);
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-pretty text-xl font-semibold">My Files</h2>
          <p className="text-sm text-muted-foreground">
            Browse, search, preview, and manage your files.
          </p>
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
                  Big Icon View
                </div>
              </SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      {/* File Display */}
      <FileTable viewMode={viewMode} />
    </div>
  );
}
