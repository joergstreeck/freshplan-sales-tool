import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card-transition';

export function CalculatorResultsSkeleton() {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          Ihr Rabatt-Ergebnis
          <div className="h-8 w-20 bg-muted animate-pulse rounded" />
        </CardTitle>
        <CardDescription>
          <div className="h-4 w-48 bg-muted animate-pulse rounded" />
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Hauptergebnis */}
        <div className="text-center p-6 bg-primary/5 rounded-lg border-2 border-primary/20">
          <div className="space-y-3">
            <div className="h-4 w-32 bg-muted animate-pulse rounded mx-auto" />
            <div className="h-10 w-40 bg-muted animate-pulse rounded mx-auto" />
            <div className="h-6 w-56 bg-muted animate-pulse rounded mx-auto" />
          </div>
        </div>

        {/* Rabatt-Aufschlüsselung */}
        <div className="space-y-3">
          <div className="h-4 w-32 bg-muted animate-pulse rounded" />

          <div className="space-y-2">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="flex items-center justify-between p-3 rounded-md bg-muted/50">
                <div className="flex-1 space-y-2">
                  <div className="h-4 w-24 bg-muted animate-pulse rounded" />
                  <div className="h-3 w-48 bg-muted animate-pulse rounded" />
                </div>
                <div className="h-5 w-12 bg-muted animate-pulse rounded" />
              </div>
            ))}
          </div>

          {/* Gesamtsumme */}
          <div className="pt-3 border-t">
            <div className="flex items-center justify-between p-3 rounded-md bg-primary/10 border border-primary/20">
              <div className="flex-1 space-y-2">
                <div className="h-5 w-28 bg-muted animate-pulse rounded" />
                <div className="h-3 w-32 bg-muted animate-pulse rounded" />
              </div>
              <div className="h-6 w-16 bg-muted animate-pulse rounded" />
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
