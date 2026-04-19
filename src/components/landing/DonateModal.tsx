import { useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import Icon from "@/components/ui/icon"
import type { DonateRank } from "@/types"

interface DonateModalProps {
  rank: DonateRank | null
  onClose: () => void
}

export default function DonateModal({ rank, onClose }: DonateModalProps) {
  const [nick, setNick] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  if (!rank) return null

  const handleSubmit = async () => {
    if (!nick.trim()) {
      setError("Введи свой ник в Minecraft")
      return
    }
    if (nick.length < 3 || nick.length > 16) {
      setError("Ник должен быть от 3 до 16 символов")
      return
    }
    setError("")
    setLoading(true)
    try {
      alert("Оплата будет подключена после добавления ключей ЮKassa!")
    } finally {
      setLoading(false)
    }
  }

  return (
    <AnimatePresence>
      <motion.div
        className="fixed inset-0 z-50 flex items-center justify-center p-4"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
      >
        <motion.div
          className="absolute inset-0 bg-black/70 backdrop-blur-sm"
          onClick={onClose}
        />
        <motion.div
          className="relative z-10 w-full max-w-md rounded-2xl border border-white/10 bg-[#0f0f0f] p-6 shadow-2xl"
          initial={{ scale: 0.9, opacity: 0, y: 20 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.9, opacity: 0, y: 20 }}
          transition={{ duration: 0.3 }}
        >
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-neutral-500 hover:text-white transition-colors"
          >
            <Icon name="X" size={20} />
          </button>

          <div className="flex items-center gap-4 mb-6">
            <div
              className="w-16 h-16 rounded-xl overflow-hidden border-2 flex-shrink-0"
              style={{ borderColor: rank.color + '88' }}
            >
              <img src={rank.image} alt={rank.name} className="w-full h-full object-cover" />
            </div>
            <div>
              <p className="text-neutral-400 text-sm">Выбранный ранг</p>
              <h3 className="text-white font-bold text-xl">{rank.name}</h3>
              <p className="font-bold text-lg" style={{ color: rank.color }}>{rank.price} ₽</p>
            </div>
          </div>

          <div className="mb-2">
            <ul className="flex flex-wrap gap-2 mb-6">
              {rank.perks.map((perk, i) => (
                <li
                  key={i}
                  className="flex items-center gap-1.5 text-xs text-neutral-300 bg-white/5 rounded-lg px-3 py-1.5"
                >
                  <Icon name="Check" size={10} style={{ color: rank.color }} />
                  {perk}
                </li>
              ))}
            </ul>
          </div>

          <div className="mb-4">
            <label className="block text-sm text-neutral-400 mb-2">
              Твой ник в Minecraft
            </label>
            <Input
              placeholder="Например: Steve123"
              value={nick}
              onChange={e => { setNick(e.target.value); setError("") }}
              className="bg-white/5 border-white/10 text-white placeholder:text-neutral-600 focus:border-green-500"
              maxLength={16}
            />
            {error && (
              <p className="text-red-400 text-xs mt-1.5 flex items-center gap-1">
                <Icon name="AlertCircle" size={12} />
                {error}
              </p>
            )}
            <p className="text-neutral-600 text-xs mt-1.5">
              Донат будет выдан автоматически на этот ник после оплаты
            </p>
          </div>

          <Button
            className="w-full font-semibold text-black text-base py-5"
            style={{ backgroundColor: rank.color }}
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <Icon name="Loader2" size={16} className="animate-spin" />
                Создаём платёж...
              </span>
            ) : (
              <span className="flex items-center gap-2">
                <Icon name="CreditCard" size={16} />
                Оплатить {rank.price} ₽
              </span>
            )}
          </Button>

          <p className="text-center text-neutral-600 text-xs mt-3">
            Оплата через ЮKassa · Безопасно
          </p>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}
