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
  const [error, setError] = useState("")
  const [copied, setCopied] = useState(false)
  const [step, setStep] = useState<'form' | 'instruction'>('form')

  if (!rank) return null

  const donateText = `Ранг ${rank.name} для ${nick.trim()}`

  const handleSubmit = () => {
    if (!nick.trim()) {
      setError("Введи свой ник в Minecraft")
      return
    }
    if (nick.length < 3 || nick.length > 16) {
      setError("Ник должен быть от 3 до 16 символов")
      return
    }
    setError("")
    navigator.clipboard.writeText(donateText).then(() => setCopied(true)).catch(() => setCopied(false))
    setStep('instruction')
  }

  const handleOpenDA = () => {
    window.open(`https://www.donationalerts.com/r/Shebls`, '_blank')
  }

  const handleCopyAgain = () => {
    navigator.clipboard.writeText(donateText).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }).catch(() => setCopied(false))
  }

  const handleBack = () => {
    setStep('form')
    setCopied(false)
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

          <AnimatePresence mode="wait">
            {step === 'form' ? (
              <motion.div
                key="form"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.2 }}
              >
                <ul className="flex flex-wrap gap-2 mb-5">
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

                <div className="mb-5">
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
                </div>

                <Button
                  className="w-full font-semibold text-black text-base py-5"
                  style={{ backgroundColor: rank.color }}
                  onClick={handleSubmit}
                >
                  <span className="flex items-center gap-2">
                    <Icon name="Heart" size={16} />
                    Задонатить {rank.price} ₽
                  </span>
                </Button>
              </motion.div>
            ) : (
              <motion.div
                key="instruction"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.2 }}
              >
                <div className="bg-green-500/10 border border-green-500/30 rounded-xl p-4 mb-4">
                  <div className="flex items-center gap-2 mb-2">
                    <Icon name="ClipboardCheck" size={16} className="text-green-400" />
                    <p className="text-green-400 text-sm font-semibold">Текст скопирован!</p>
                  </div>
                  <p className="text-white text-sm font-mono bg-black/30 rounded-lg px-3 py-2 break-all">
                    {donateText}
                  </p>
                  <button
                    onClick={handleCopyAgain}
                    className="mt-2 text-xs text-neutral-500 hover:text-neutral-300 transition-colors flex items-center gap-1"
                  >
                    <Icon name="Copy" size={11} />
                    {copied ? "Скопировано!" : "Скопировать снова"}
                  </button>
                </div>

                <div className="space-y-3 mb-5">
                  {[
                    { icon: "MousePointerClick", color: "text-blue-400", text: <>Нажми <span className="text-white font-semibold">"Открыть DonationAlerts"</span> ниже</> },
                    { icon: "RussianRuble", color: "text-green-400", text: <>Укажи сумму <span className="text-white font-semibold">{rank.price} ₽</span> (или больше)</> },
                    { icon: "ClipboardPaste", color: "text-yellow-400", text: <>В поле <span className="text-white font-semibold">"Сообщение"</span> вставь текст (Ctrl+V)</> },
                    { icon: "Clock", color: "text-pink-400", text: <>Ранг выдаётся администратором <span className="text-white font-semibold">в течение 2 дней</span></> },
                  ].map((item, i) => (
                    <div key={i} className="flex items-start gap-2.5">
                      <div className={`w-5 h-5 rounded-full bg-white/5 flex items-center justify-center flex-shrink-0 mt-0.5 ${item.color}`}>
                        <Icon name={item.icon} size={11} />
                      </div>
                      <p className="text-neutral-400 text-xs leading-relaxed">{item.text}</p>
                    </div>
                  ))}
                </div>

                <Button
                  className="w-full font-semibold text-black text-base py-5 mb-2"
                  style={{ backgroundColor: rank.color }}
                  onClick={handleOpenDA}
                >
                  <span className="flex items-center gap-2">
                    <Icon name="ExternalLink" size={16} />
                    Открыть DonationAlerts
                  </span>
                </Button>

                <button
                  onClick={handleBack}
                  className="w-full text-xs text-neutral-600 hover:text-neutral-400 transition-colors py-1"
                >
                  ← Назад
                </button>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}