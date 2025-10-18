import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

export default function SharePage({ params }: { params: { id: string } }) {
  // In this mock app we can't fetch without auth.
  // Provide a simple confirmation and placeholder preview.
  return (
    <div className="grid min-h-dvh place-items-center p-4">
      <Card className="w-full max-w-xl">
        <CardHeader>
          <CardTitle>Shared File</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <img src="/shared-file.jpg" alt="" className="w-full rounded-lg" />
          <p className="text-sm text-muted-foreground">
            Public link for file ID: <code>{params.id}</code>
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
