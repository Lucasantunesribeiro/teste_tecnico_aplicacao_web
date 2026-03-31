import { useState } from 'react'
import { PlusCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { EnderecoList } from '@/features/enderecos/components/EnderecoList'
import { EnderecoForm } from '@/features/enderecos/components/EnderecoForm'
import { useAuthStore } from '@/features/auth/store/authStore'

export function EnderecosPage() {
  const [open, setOpen] = useState(false)
  const user = useAuthStore((s) => s.user)

  if (!user) return null

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Meus Endereços</h1>
          <p className="text-muted-foreground">Gerencie seus endereços</p>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>
              <PlusCircle className="h-4 w-4 mr-2" />
              Novo Endereço
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Adicionar Endereço</DialogTitle>
            </DialogHeader>
            <EnderecoForm usuarioId={user.id} onSuccess={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>
      <EnderecoList usuarioId={user.id} />
    </div>
  )
}
