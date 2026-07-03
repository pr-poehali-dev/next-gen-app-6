import { useRef, useState, useCallback } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import Section from './Section'
import Layout from './Layout'
import CartDrawer from './CartDrawer'
import { sections } from './sections'
import { useCart } from '@/hooks/useCart'
import Icon from '@/components/ui/icon'

const slideVariants = {
  enter: (dir: number) => ({
    y: dir > 0 ? '100%' : '-100%',
    opacity: 0,
  }),
  center: {
    y: 0,
    opacity: 1,
  },
  exit: (dir: number) => ({
    y: dir > 0 ? '-100%' : '100%',
    opacity: 0,
  }),
}

export default function LandingPage() {
  const [activeSection, setActiveSection] = useState(0)
  const [direction, setDirection] = useState(1)
  const [cartOpen, setCartOpen] = useState(false)
  const dragStartY = useRef(0)
  const ranksScrollRef = useRef<HTMLDivElement | null>(null)
  const wheelCooldown = useRef(false)
  const { items, badge, addItem, removeItem, clearBadge, total } = useCart()

  const setRanksRef = useCallback((el: HTMLDivElement | null) => {
    ranksScrollRef.current = el
    if (el) {
      requestAnimationFrame(() => {
        el.scrollTop = el.scrollHeight
      })
    }
  }, [])

  const goTo = (index: number) => {
    if (index === activeSection) return
    setDirection(index > activeSection ? 1 : -1)
    setActiveSection(index)
  }

  const goNext = () => { if (activeSection < sections.length - 1) goTo(activeSection + 1) }
  const goPrev = () => { if (activeSection > 0) goTo(activeSection - 1) }

  const handleWheel = (e: React.WheelEvent) => {
    const ranksEl = ranksScrollRef.current
    if (ranksEl) {
      const rect = ranksEl.getBoundingClientRect()
      const inRanks = e.clientX >= rect.left && e.clientX <= rect.right && e.clientY >= rect.top && e.clientY <= rect.bottom
      if (inRanks) return
    }
    if (wheelCooldown.current) return
    if (e.deltaY > 40) { goNext(); wheelCooldown.current = true; setTimeout(() => { wheelCooldown.current = false }, 800) }
    else if (e.deltaY < -40) { goPrev(); wheelCooldown.current = true; setTimeout(() => { wheelCooldown.current = false }, 800) }
  }

  const handleTouchStart = (e: React.TouchEvent) => {
    dragStartY.current = e.touches[0].clientY
  }

  const handleTouchEnd = (e: React.TouchEvent) => {
    const diff = dragStartY.current - e.changedTouches[0].clientY
    if (diff > 50) goNext()
    else if (diff < -50) goPrev()
  }

  const handleOpenCart = () => {
    clearBadge()
    setCartOpen(true)
  }

  const navSections = sections.filter(s => !s.freeScroll)

  return (
    <Layout>
      <nav className="fixed top-0 right-0 h-screen flex flex-col justify-center z-30 p-4">
        {navSections.map((section) => {
          const index = sections.indexOf(section)
          return (
            <button
              key={section.id}
              className={`w-3 h-3 rounded-full my-2 transition-all ${
                index === activeSection ? 'bg-white scale-150' : 'bg-gray-600'
              }`}
              onClick={() => goTo(index)}
            />
          )
        })}
      </nav>

      <button
        onClick={handleOpenCart}
        className="fixed top-5 left-5 z-40 w-12 h-12 rounded-2xl bg-purple-600 hover:bg-purple-700 transition-colors flex items-center justify-center shadow-lg shadow-purple-900/40"
      >
        <Icon name="ShoppingCart" size={20} className="text-white" />
        <AnimatePresence>
          {badge > 0 && (
            <motion.span
              key={badge}
              className="absolute -top-1.5 -right-1.5 min-w-[22px] h-[22px] bg-red-500 text-white text-xs font-bold rounded-full flex items-center justify-center px-1 shadow"
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0 }}
              transition={{ type: 'spring', stiffness: 400, damping: 15 }}
            >
              {badge}
            </motion.span>
          )}
        </AnimatePresence>
      </button>

      <div
        className="h-full overflow-hidden relative"
        onWheel={handleWheel}
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
      >
        <AnimatePresence initial={false} custom={direction} mode="wait">
          <motion.div
            key={activeSection}
            custom={direction}
            variants={slideVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.45, ease: [0.77, 0, 0.175, 1] }}
            className="absolute inset-0 w-full h-full"
          >
            <Section
              {...sections[activeSection]}
              isActive={true}
              onButtonClick={sections[activeSection].showButton ? () => {
                const donateIndex = sections.findIndex(s => s.id === 'donate')
                if (donateIndex !== -1) goTo(donateIndex)
              } : undefined}
              onAddToCart={addItem}
              onRanksRef={setRanksRef}
            />
          </motion.div>
        </AnimatePresence>
      </div>

      <AnimatePresence>
        {cartOpen && (
          <CartDrawer
            items={items}
            total={total}
            onRemove={removeItem}
            onClose={() => setCartOpen(false)}
          />
        )}
      </AnimatePresence>
    </Layout>
  )
}