"use client";

import { useState, useEffect } from "react";
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
import {
  Copy,
  LinkIcon,
  Trash2,
} from "lucide-react";

export function LinkFormDialog({
  open,
  onOpenChange,
  fileId,
  existingLink,
  onLinkDeleted,
  onLinkGenerated,
}: Readonly<{
  open: boolean;
  onOpenChange: (v: boolean) => void;
  fileId: string;
  existingLink?: {
    id: string;
    url: string;
    expiresAt: string | null;
    hasPassword: boolean;
  } | null;
  onLinkDeleted?: () => void;
  onLinkGenerated?: () => void;
}>) {
  const [passwordEnabled, setPasswordEnabled] = useState(false);
  const [password, setPassword] = useState("");

  const [expiresEnabled, setExpiresEnabled] = useState(false);
  const [expiryDays, setExpiryDays] = useState(0);
  const [expiryHours, setExpiryHours] = useState(0);
  const [expiryMinutes, setExpiryMinutes] = useState(30);

  const [calculatedExpiry, setCalculatedExpiry] = useState<Date | null>(null);

  const [loading, setLoading] = useState(false);
  const [generatedUrl, setGeneratedUrl] = useState(null);

  const activeUrl = existingLink?.url ?? generatedUrl;

  useEffect(() => {
    if (!expiresEnabled) return setCalculatedExpiry(null);

    const totalMs =
      expiryDays * 86400000 +
      expiryHours * 3600000 +
      expiryMinutes * 60000;

    if (totalMs <= 0) return setCalculatedExpiry(null);

    setCalculatedExpiry(new Date(Date.now() + totalMs));
  }, [expiresEnabled, expiryDays, expiryHours, expiryMinutes]);

  const setPreset = (ms: number) => {
    setExpiresEnabled(true);
    setExpiryDays(Math.floor(ms / 86400000));
    setExpiryHours(Math.floor((ms % 86400000) / 3600000));
    setExpiryMinutes(Math.floor((ms % 3600000) / 60000));
  };

  async function handleGenerate() {
    try {
      setLoading(true);

      let expiresAt = null;

      if (expiresEnabled) {
        const totalMs =
          expiryDays * 86400000 +
          expiryHours * 3600000 +
          expiryMinutes * 60000;

        expiresAt = new Date(Date.now() + totalMs).toISOString();
      }

      const res = await api("/links/generate", {
        method: "POST",
        body: JSON.stringify({
          fileId,
          password: passwordEnabled ? password : null,
          expiresAt,
        }),
      });

      setGeneratedUrl(res.url);
      onLinkGenerated?.();
      navigator.clipboard.writeText(res.url);

      toast.success("Share link generated & copied");
    } catch (e: any) {
      toast.error("Failed to generate link", { description: e.message });
    } finally {
      setLoading(false);
    }
  }

  async function handleDeleteLink() {
    try {
      await api(`/links/delete/${existingLink?.id}`, { method: "DELETE" });
      onLinkDeleted?.();
      toast.success("Link deleted");
      onOpenChange(false);
    } catch {
      toast.error("Failed to delete link");
    }
  }

  function resetForm() {
    setPasswordEnabled(false);
    setPassword("");
    setExpiresEnabled(false);
    setExpiryDays(0);
    setExpiryHours(0);
    setExpiryMinutes(30);
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
      <DialogContent className="sm:max-w-lg p-0">
        <DialogHeader className="px-6 pb-2 pt-6 border-b">
          <DialogTitle className="text-xl font-semibold tracking-tight">
            {existingLink ? "Active Share Link" : "Generate Share Link"}
          </DialogTitle>
        </DialogHeader>
        <div className="p-6 space-y-8">

          {activeUrl ? (
            <div className="space-y-6">
              <div className="rounded-md border bg-muted/40 p-4">
                <div className="text-sm font-medium break-all">{activeUrl}</div>
              </div>
              <div className="space-y-1 text-sm text-muted-foreground">
                {existingLink?.expiresAt && (
                  <p>
                    Expires on:{" "}
                    <span className="font-medium text-foreground">
                      {new Date(existingLink.expiresAt).toLocaleString()}
                    </span>
                  </p>
                )}

                {existingLink?.hasPassword && (
                  <p className="text-yellow-600 font-medium">
                    🔒 Password protected
                  </p>
                )}
              </div>
              <div className="flex justify-end gap-2">
                <Button
                  variant="outline"
                  onClick={() => {
                    navigator.clipboard.writeText(activeUrl);
                    toast.success("Copied!");
                  }}
                  className="gap-2"
                >
                  <Copy className="h-4 w-4" /> Copy
                </Button>

                <Button onClick={() => window.open(activeUrl, "_blank")} className="gap-2">
                  <LinkIcon className="h-4 w-4" /> Open
                </Button>
              </div>
              {existingLink && (
                <div className="pt-4 border-t flex justify-end">
                  <Button
                    variant="destructive"
                    onClick={handleDeleteLink}
                    className="gap-2"
                  >
                    <Trash2 className="h-4 w-4" /> Delete Link
                  </Button>
                </div>
              )}
            </div>
          ) : (
            <div className="space-y-10">
              <div className="border rounded-lg bg-muted/30 p-5 space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-semibold tracking-wider text-muted-foreground">
                      Password Protection
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Require a password before file can be accessed.
                    </p>
                  </div>

                  <Switch
                    checked={passwordEnabled}
                    onCheckedChange={setPasswordEnabled}
                  />
                </div>

                {passwordEnabled && (
                  <Input
                    placeholder="Enter password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                )}
              </div>
              <div className="border rounded-lg bg-muted/30 p-5 space-y-5">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-semibold tracking-wider text-muted-foreground">
                      Expiry
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Automatically disable link after selected time.
                    </p>
                  </div>

                  <Switch
                    checked={expiresEnabled}
                    onCheckedChange={setExpiresEnabled}
                  />
                </div>

                {expiresEnabled && (
                  <>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPreset(3600000)}
                      >
                        1 hour
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPreset(24 * 3600000)}
                      >
                        24 hours
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPreset(7 * 24 * 3600000)}
                      >
                        7 days
                      </Button>
                    </div>
                    <div className="grid grid-cols-3 gap-4">
                      <div>
                        <Label className="text-xs">Days</Label>
                        <Input
                          type="number"
                          min={0}
                          value={expiryDays}
                          onChange={(e) => setExpiryDays(Number(e.target.value))}
                        />
                      </div>

                      <div>
                        <Label className="text-xs">Hours</Label>
                        <Input
                          type="number"
                          min={0}
                          value={expiryHours}
                          onChange={(e) => setExpiryHours(Number(e.target.value))}
                        />
                      </div>

                      <div>
                        <Label className="text-xs">Minutes</Label>
                        <Input
                          type="number"
                          min={0}
                          value={expiryMinutes}
                          onChange={(e) =>
                            setExpiryMinutes(Number(e.target.value))
                          }
                        />
                      </div>
                    </div>

                    {calculatedExpiry && (
                      <p className="text-xs text-muted-foreground pt-2">
                        Link will be available until:{" "}
                        <span className="font-medium text-foreground">
                          {calculatedExpiry.toLocaleString()}
                        </span>
                      </p>
                    )}
                  </>
                )}
              </div>
              <Button className="w-full py-5 text-base" disabled={loading} onClick={handleGenerate}>
                {loading ? "Generating..." : "Generate Share Link"}
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
