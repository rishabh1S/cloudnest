"use client";

import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { toast } from "sonner";
import { api } from "@/lib/api";
import { Copy, Link as LinkIcon } from "lucide-react";
import {
  Select,
  SelectTrigger,
  SelectContent,
  SelectItem,
  SelectValue,
} from "@/components/ui/select";

export function LinkFormDialog({
  open,
  onOpenChange,
  fileId,
}: Readonly<{
  open: boolean;
  onOpenChange: (v: boolean) => void;
  fileId: string;
}>) {
  const [passwordEnabled, setPasswordEnabled] = useState(false);
  const [password, setPassword] = useState("");
  const [expiresEnabled, setExpiresEnabled] = useState(false);
  const [expiryValue, setExpiryValue] = useState<number>(10); // default 10
  const [expiryUnit, setExpiryUnit] = useState<"minutes" | "hours" | "days">(
    "minutes"
  );
  const [loading, setLoading] = useState(false);
  const [generatedUrl, setGeneratedUrl] = useState<string | null>(null);

  async function handleGenerate() {
    try {
      setLoading(true);

      let expiresAt: string | null = null;
      if (expiresEnabled) {
        let totalMinutes = expiryValue;
        if (expiryUnit === "hours") totalMinutes = expiryValue * 60;
        if (expiryUnit === "days") totalMinutes = expiryValue * 24 * 60;

        const expiryDate = new Date(Date.now() + totalMinutes * 60 * 1000);
        expiresAt = expiryDate.toISOString();
      }

      const body = {
        fileId,
        password: passwordEnabled && password ? password : null,
        expiresAt,
      };

      const res = await api("/links/generate", {
        method: "POST",
        body: JSON.stringify(body),
      });

      setGeneratedUrl(res.url);
      await navigator.clipboard.writeText(res.url);
      toast.success("Shareable link generated and copied!");
    } catch (e: any) {
      toast.error("Failed to generate link", { description: e.message });
    } finally {
      setLoading(false);
    }
  }

  function resetForm() {
    setPasswordEnabled(false);
    setPassword("");
    setExpiresEnabled(false);
    setExpiryValue(10);
    setExpiryUnit("minutes");
    setGeneratedUrl(null);
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(v) => {
        onOpenChange(v);
        if (!v) resetForm();
      }}
    >
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Generate Shareable Link</DialogTitle>
        </DialogHeader>

        {generatedUrl ? (
          <div className="space-y-4">
            <div className="bg-muted p-3 rounded-md text-sm break-all">
              {generatedUrl}
            </div>

            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  navigator.clipboard.writeText(generatedUrl);
                  toast.success("Copied to clipboard!");
                }}
              >
                <Copy className="w-4 h-4 mr-1" /> Copy
              </Button>
              <Button
                variant="default"
                onClick={() => window.open(generatedUrl, "_blank")}
              >
                <LinkIcon className="w-4 h-4 mr-1" /> Open Link
              </Button>
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Password Section */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label>Password Protection</Label>
                <Switch
                  checked={passwordEnabled}
                  onCheckedChange={setPasswordEnabled}
                />
              </div>
              {passwordEnabled && (
                <Input
                  type="text"
                  placeholder="Enter password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              )}
            </div>

            {/* Expiry Section */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <Label>Set Expiry</Label>
                <Switch
                  checked={expiresEnabled}
                  onCheckedChange={setExpiresEnabled}
                />
              </div>

              {expiresEnabled && (
                <div className="flex justify-between items-center gap-4">
                  <Input
                    type="number"
                    min={1}
                    value={expiryValue}
                    onChange={(e) => setExpiryValue(Number(e.target.value))}
                  />
                  <Select
                    value={expiryUnit}
                    onValueChange={(v) =>
                      setExpiryUnit(v as "minutes" | "hours" | "days")
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Unit" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="minutes">Minutes</SelectItem>
                      <SelectItem value="hours">Hours</SelectItem>
                      <SelectItem value="days">Days</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}
            </div>

            <Button
              onClick={handleGenerate}
              disabled={loading}
              className="w-full"
            >
              {loading ? "Generating..." : "Generate Link"}
            </Button>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
