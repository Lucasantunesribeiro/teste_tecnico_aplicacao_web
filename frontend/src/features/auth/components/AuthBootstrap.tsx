import { type PropsWithChildren, useEffect } from 'react'
import { authService } from '../services/authService'
import { useAuthStore } from '../store/authStore'

export function AuthBootstrap({ children }: PropsWithChildren) {
  const isBootstrapping = useAuthStore((state) => state.isBootstrapping)
  const hasHydratedSession = useAuthStore((state) => state.hasHydratedSession)
  const hydrate = useAuthStore((state) => state.hydrate)

  useEffect(() => {
    if (hasHydratedSession) {
      return
    }

    let active = true

    authService
      .me()
      .then((response) => {
        if (active) {
          hydrate(response)
        }
      })
      .catch(() => {
        if (active) {
          hydrate(null)
        }
      })

    return () => {
      active = false
    }
  }, [hasHydratedSession, hydrate])

  if (isBootstrapping) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.18),_transparent_34%),linear-gradient(180deg,_#f8fafc_0%,_#eef2ff_100%)]">
        <div className="rounded-2xl border border-white/70 bg-white/90 px-6 py-5 shadow-xl backdrop-blur">
          <div className="flex items-center gap-3 text-sm text-slate-600">
            <div className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            Validando sua sessão...
          </div>
        </div>
      </div>
    )
  }

  return children
}
