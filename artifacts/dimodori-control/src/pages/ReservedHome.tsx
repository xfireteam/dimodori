export default function ReservedHome() {
  return (
    <main className="flex min-h-screen flex-col bg-background px-6 text-foreground">
      <div className="flex flex-1 items-center justify-center py-6">
        <div className="flex flex-col items-center gap-5 text-center">
          <img
            src={`${import.meta.env.BASE_URL.replace(/\/$/, '')}/dimodori_logo.png`}
            alt="DIMODORI"
            className="h-20 w-20 object-contain drop-shadow-[0_0_15px_rgba(37,99,235,0.35)]"
          />
          <h1 className="text-2xl font-bold tracking-tight">DIMODORI</h1>
        </div>
      </div>

      <footer className="border-t border-border/60 py-5 text-center text-sm text-muted-foreground">
        Powered by{' '}
        <a
          href="https://wa.me/17869365291"
          target="_blank"
          rel="noopener noreferrer"
          className="font-medium text-foreground underline-offset-4 transition-colors hover:text-primary hover:underline focus-visible:rounded-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          Rich Enginner
        </a>
      </footer>
    </main>
  );
}