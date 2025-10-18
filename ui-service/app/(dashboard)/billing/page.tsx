import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"

export default function BillingPage() {
  return (
    <div className="mx-auto grid max-w-3xl gap-6 md:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>Free</CardTitle>
          <CardDescription>For personal use</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <ul className="list-inside list-disc text-sm text-muted-foreground">
            <li>5 GB storage</li>
            <li>Basic support</li>
          </ul>
          <Button variant="outline" disabled>
            Current Plan
          </Button>
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle>Pro</CardTitle>
          <CardDescription>For teams and power users</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <ul className="list-inside list-disc text-sm text-muted-foreground">
            <li>1 TB storage</li>
            <li>Priority support</li>
          </ul>
          <Button>Upgrade with Stripe</Button>
        </CardContent>
      </Card>
    </div>
  )
}
