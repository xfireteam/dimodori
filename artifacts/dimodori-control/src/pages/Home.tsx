import { format } from "date-fns"
import { AlertTriangle, Server, Shield, RefreshCw } from "lucide-react"
import { Switch } from "@/components/ui/switch"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useGetServerSelectorConfig, useUpdateServerSelectorConfig, getGetServerSelectorConfigQueryKey } from "@workspace/api-client-react"
import { useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import { cn } from "@/lib/utils"

export default function Home() {
  const queryClient = useQueryClient()
  
  const { 
    data: config, 
    isLoading: isConfigLoading, 
    isError: isConfigError, 
    refetch: refetchConfig
  } = useGetServerSelectorConfig({
    query: {
      queryKey: getGetServerSelectorConfigQueryKey()
    }
  })
  
  const updateConfig = useUpdateServerSelectorConfig()
  
  const handleToggle = (checked: boolean) => {
    const previousConfig = queryClient.getQueryData(getGetServerSelectorConfigQueryKey())
    
    queryClient.setQueryData(getGetServerSelectorConfigQueryKey(), {
      hideServerSelector: checked,
      updatedAt: new Date().toISOString()
    })

    updateConfig.mutate({ data: { hideServerSelector: checked } }, {
      onSuccess: (newConfig) => {
        queryClient.setQueryData(getGetServerSelectorConfigQueryKey(), newConfig)
        toast.success("Configuración actualizada", {
          description: "Los cambios se aplicarán inmediatamente en la app.",
        })
      },
      onError: () => {
        if (previousConfig) {
          queryClient.setQueryData(getGetServerSelectorConfigQueryKey(), previousConfig)
        }
        toast.error("Error al actualizar", {
          description: "No se pudo cambiar la configuración. Revisa tu conexión.",
        })
      }
    })
  }

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col items-center justify-center p-4 sm:p-8 selection:bg-primary/30">
      <div className="fixed inset-0 pointer-events-none opacity-20" 
           style={{ backgroundImage: 'radial-gradient(circle at center, rgba(255,255,255,0.1) 1px, transparent 1px)', backgroundSize: '32px 32px' }}>
      </div>

      <div className="w-full max-w-lg space-y-8 relative z-10">
        
        <div className="flex flex-col items-center space-y-6 text-center">
          <div className="relative group">
            <div className="absolute -inset-4 bg-primary/20 blur-2xl rounded-full opacity-0 group-hover:opacity-100 transition duration-1000"></div>
            <img 
              src={`${import.meta.env.BASE_URL.replace(/\/$/, '')}/dimodori_logo.png`}
              alt="DIMODORI Logo" 
              className="w-24 h-24 object-contain relative z-10 drop-shadow-[0_0_15px_rgba(37,99,235,0.5)]"
            />
          </div>
          <h1 className="text-2xl font-bold tracking-tight">
            DIMODORI Control
          </h1>
        </div>

        <div className="border border-border bg-card/60 backdrop-blur-xl rounded-xl overflow-hidden shadow-2xl relative">
          
          <div className="px-6 py-4 border-b border-border bg-black/60 flex items-center justify-between">
            <div className="text-sm font-medium text-muted-foreground">
              Estado
            </div>
            
            <div className="flex items-center gap-2 text-sm font-medium">
               {isConfigLoading ? (
                 <span className="flex items-center gap-2 text-muted-foreground">
                   <RefreshCw className="w-3 h-3 animate-spin" /> Cargando
                 </span>
               ) : isConfigError ? (
                 <span className="text-destructive flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-destructive animate-pulse"></span> Sin conexión
                 </span>
               ) : (
                 <span className="flex items-center gap-2 text-primary drop-shadow-[0_0_8px_rgba(37,99,235,0.8)]">
                   <span className="relative flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
                  </span>
                  Conectado
                 </span>
               )}
            </div>
          </div>

          <div className="p-6 sm:p-8 space-y-8">
            
            {isConfigError ? (
              <div className="p-6 border border-destructive/30 bg-destructive/5 rounded-lg text-center space-y-5">
                <AlertTriangle className="w-10 h-10 text-destructive mx-auto opacity-80" />
                <p className="text-sm text-destructive font-medium">No se pudo cargar la configuración.</p>
                <button 
                  onClick={() => refetchConfig()} 
                  className="text-sm bg-destructive text-destructive-foreground px-6 py-2.5 rounded font-medium hover:bg-destructive/90 transition-colors"
                >
                  Reintentar conexión
                </button>
              </div>
            ) : (
              <div className="space-y-8 animate-in fade-in slide-in-from-bottom-2 duration-700">
                
                <div className="flex items-center justify-between gap-6">
                  <div className="space-y-1 flex-1">
                    <label 
                      htmlFor="server-selector-switch"
                      className="text-lg font-medium text-foreground tracking-tight cursor-pointer"
                    >
                      Ocultar selector de servidores
                    </label>
                  </div>
                  <div className="relative shrink-0">
                    {updateConfig.isPending && (
                      <div className="absolute -inset-2 bg-primary/20 blur-xl rounded-full animate-pulse"></div>
                    )}
                    <Switch
                      id="server-selector-switch"
                      disabled={isConfigLoading || updateConfig.isPending}
                      checked={config?.hideServerSelector ?? false}
                      onCheckedChange={handleToggle}
                      className="relative z-10 scale-125 origin-right"
                    />
                  </div>
                </div>

                <div className={cn(
                  "p-5 rounded-lg border transition-all duration-700",
                  config?.hideServerSelector 
                    ? "bg-primary/10 border-primary/30" 
                    : "bg-muted/30 border-transparent"
                )}>
                  <div className="flex items-start gap-4">
                    <Server className={cn(
                      "w-5 h-5 mt-0.5 transition-colors duration-700 shrink-0",
                      config?.hideServerSelector ? "text-primary drop-shadow-[0_0_5px_rgba(37,99,235,0.8)]" : "text-muted-foreground"
                    )} />
                    <div className="space-y-3">
                      <p className="text-sm leading-relaxed text-foreground/90">
                        {config?.hideServerSelector 
                          ? "El selector de servidores está oculto. Sólo el modo de conexión manual permanece visible en DIMODORI."
                          : "El selector de servidores está activo. La lista de servidores y el modo manual permanecen visibles en DIMODORI."
                        }
                      </p>
                      {config?.updatedAt && (
                        <p className="text-xs text-muted-foreground/60 pt-3 border-t border-border/50">
                          Último cambio: {format(new Date(config.updatedAt), "yyyy-MM-dd HH:mm:ss")}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        <Alert className="bg-transparent border-muted/50 text-muted-foreground/80 text-xs leading-relaxed px-4 py-4">
          <Shield className="w-4 h-4 text-muted-foreground/60" />
          <AlertDescription className="pl-3 mt-0.5">
            Este panel es público y cualquier visitante puede cambiar esta configuración. Su propósito es control de emergencia sin fricción. No requiere login.
          </AlertDescription>
        </Alert>
        
      </div>
    </div>
  )
}