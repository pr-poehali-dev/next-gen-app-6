import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import Icon from '@/components/ui/icon'
import type { CartItem } from '@/hooks/useCart'

interface CartDrawerProps {
  items: CartItem[]
  total: number
  onRemove: (index: number) => void
  onClose: () => void
}

const BG = 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/8569132f-0ed8-49ef-9161-cab6af80f160.jpg'

export default function CartDrawer({ items, total, onRemove, onClose }: CartDrawerProps) {
  const [nick, setNick] = useState('')
  const [error, setError] = useState('')
  const [step, setStep] = useState<'cart' | 'instruction'>('cart')
  const [copied, setCopied] = useState(false)

  const donateText = items.map(i => `[${i.rank.name}]`).join(', ') + ` для ${nick.trim()}`

  const handleCheckout = () => {
    if (!nick.trim()) { setError('Введи свой ник в Minecraft'); return }
    if (nick.length < 3 || nick.length > 16) { setError('Ник должен быть от 3 до 16 символов'); return }
    setError('')
    navigator.clipboard.writeText(donateText).then(() => setCopied(true)).catch(() => setCopied(false))
    setStep('instruction')
  }

  const handleCopyAgain = () => {
    navigator.clipboard.writeText(donateText).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }).catch(() => setCopied(false))
  }

  return (
    <motion.div
      className="fixed inset-0 z-50 flex"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      <motion.div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      <motion.div
        className="relative ml-auto w-full max-w-lg h-full flex flex-col overflow-hidden shadow-2xl"
        initial={{ x: '100%' }}
        animate={{ x: 0 }}
        exit={{ x: '100%' }}
        transition={{ type: 'spring', damping: 28, stiffness: 250 }}
      >
        <div
          className="absolute inset-0"
          style={{
            backgroundImage: `linear-gradient(to bottom, rgba(0,0,0,0.82) 0%, rgba(0,0,0,0.88) 100%), url(${BG})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />

        <div className="relative z-10 flex flex-col h-full">
          <div className="flex items-center justify-between px-6 py-5 border-b border-white/10">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-purple-600 flex items-center justify-center">
                <Icon name="ShoppingCart" size={18} className="text-white" />
              </div>
              <div>
                <h2 className="text-white font-bold text-lg leading-none">Корзина</h2>
                <p className="text-neutral-400 text-xs mt-0.5">{items.length} {items.length === 1 ? 'товар' : items.length < 5 ? 'товара' : 'товаров'}</p>
              </div>
            </div>
            <button onClick={onClose} className="text-neutral-500 hover:text-white transition-colors p-1">
              <Icon name="X" size={22} />
            </button>
          </div>

          <div className="flex-1 overflow-y-auto px-6 py-4">
            <AnimatePresence mode="wait">
              {step === 'cart' ? (
                <motion.div key="cart" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
                  {items.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-48 gap-3">
                      <Icon name="ShoppingCart" size={40} className="text-neutral-700" />
                      <p className="text-neutral-500 text-sm">Корзина пуста</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {items.map((item, i) => (
                        <motion.div
                          key={item.addedAt}
                          className="flex items-center gap-4 bg-white/5 border border-white/10 rounded-xl p-3"
                          initial={{ opacity: 0, x: 20 }}
                          animate={{ opacity: 1, x: 0 }}
                          exit={{ opacity: 0, x: -20 }}
                          transition={{ duration: 0.2 }}
                          layout
                        >
                          <div
                            className="w-14 h-14 rounded-xl overflow-hidden flex-shrink-0 border-2"
                            style={{ borderColor: item.rank.color + '66' }}
                          >
                            <img src={item.rank.image} alt={item.rank.name} className="w-full h-full object-cover" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <span
                              className="text-xs font-bold px-2 py-0.5 rounded-md"
                              style={{ color: item.rank.color, backgroundColor: item.rank.color + '22' }}
                            >
                              [{item.rank.name}]
                            </span>
                            <p className="text-white font-semibold text-sm mt-1">{item.rank.name}</p>
                            <p className="font-bold text-sm" style={{ color: item.rank.color }}>{item.rank.price} ₽</p>
                          </div>
                          <button
                            onClick={() => onRemove(i)}
                            className="text-neutral-600 hover:text-red-400 transition-colors p-1"
                          >
                            <Icon name="Trash2" size={16} />
                          </button>
                        </motion.div>
                      ))}
                    </div>
                  )}

                  {items.length > 0 && (
                    <div className="mt-5">
                      <label className="block text-sm text-neutral-400 mb-2">Твой ник в Minecraft</label>
                      <Input
                        placeholder="Например: Steve123"
                        value={nick}
                        onChange={e => { setNick(e.target.value); setError('') }}
                        className="bg-white/5 border-white/10 text-white placeholder:text-neutral-600 focus:border-purple-500"
                        maxLength={16}
                      />
                      {error && (
                        <p className="text-red-400 text-xs mt-1.5 flex items-center gap-1">
                          <Icon name="AlertCircle" size={12} /> {error}
                        </p>
                      )}
                    </div>
                  )}
                </motion.div>
              ) : (
                <motion.div key="instruction" initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0 }}>
                  <div className="bg-green-500/10 border border-green-500/30 rounded-xl p-4 mb-4">
                    <div className="flex items-center gap-2 mb-2">
                      <Icon name="ClipboardCheck" size={16} className="text-green-400" />
                      <p className="text-green-400 text-sm font-semibold">Текст скопирован!</p>
                    </div>
                    <p className="text-white text-sm font-mono bg-black/30 rounded-lg px-3 py-2 break-all">{donateText}</p>
                    <button
                      onClick={handleCopyAgain}
                      className="mt-2 text-xs text-neutral-500 hover:text-neutral-300 transition-colors flex items-center gap-1"
                    >
                      <Icon name="Copy" size={11} /> {copied ? 'Скопировано!' : 'Скопировать снова'}
                    </button>
                  </div>

                  <div className="space-y-3">
                    {[
                      { icon: 'MousePointerClick', color: 'text-blue-400', text: <><span className="text-white font-semibold">Открой DonationAlerts</span> по кнопке ниже</> },
                      { icon: 'RussianRuble', color: 'text-green-400', text: <>Укажи сумму <span className="text-white font-semibold">{total} ₽</span> (или больше)</> },
                      { icon: 'ClipboardPaste', color: 'text-yellow-400', text: <>В поле <span className="text-white font-semibold">"Сообщение"</span> вставь текст (Ctrl+V)</> },
                      { icon: 'Clock', color: 'text-pink-400', text: <>Ранги выдаются <span className="text-white font-semibold">в течение 2 дней</span></> },
                    ].map((item, i) => (
                      <div key={i} className="flex items-start gap-2.5">
                        <div className={`w-5 h-5 rounded-full bg-white/5 flex items-center justify-center flex-shrink-0 mt-0.5 ${item.color}`}>
                          <Icon name={item.icon} size={11} />
                        </div>
                        <p className="text-neutral-400 text-xs leading-relaxed">{item.text}</p>
                      </div>
                    ))}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {items.length > 0 && (
            <div className="relative z-10 px-6 py-5 border-t border-white/10">
              <div className="flex items-center justify-between mb-4">
                <span className="text-neutral-400 text-sm">Итого:</span>
                <span className="text-white font-bold text-xl">{total} ₽</span>
              </div>
              {step === 'cart' ? (
                <Button
                  className="w-full font-semibold text-white text-base py-5 bg-purple-600 hover:bg-purple-700"
                  onClick={handleCheckout}
                >
                  <span className="flex items-center gap-2">
                    <Icon name="Heart" size={16} />
                    Задонатить {total} ₽
                  </span>
                </Button>
              ) : (
                <>
                  <Button
                    className="w-full font-semibold text-white text-base py-5 bg-purple-600 hover:bg-purple-700 mb-2"
                    onClick={() => window.open('https://www.donationalerts.com/r/Shebls', '_blank')}
                  >
                    <span className="flex items-center gap-2">
                      <Icon name="ExternalLink" size={16} />
                      Открыть DonationAlerts
                    </span>
                  </Button>
                  <button
                    onClick={() => setStep('cart')}
                    className="w-full text-xs text-neutral-600 hover:text-neutral-400 transition-colors py-1"
                  >
                    ← Назад
                  </button>
                </>
              )}
            </div>
          )}
        </div>
      </motion.div>
    </motion.div>
  )
}
