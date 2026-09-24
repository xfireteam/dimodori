export default function ReservedHome() {
  return (
    <main className="min-h-screen bg-background text-foreground flex items-center justify-center p-6">
      <div className="flex flex-col items-center gap-5 text-center">
        <img
          src={`${import.meta.env.BASE_URL.replace(/\/$/, '')}/dimodori_logo.png`}
          alt="DIMODORI"
          className="h-20 w-20 object-contain drop-shadow-[0_0_15px_rgba(37,99,235,0.35)]"
        />
        <h1 className="text-2xl font-bold tracking-tight">DIMODORI</h1>
      </div>
    </main>
  );
}