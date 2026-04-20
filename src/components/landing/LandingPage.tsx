import { useEffect, useRef, useState } from 'react'
import { motion, useScroll, useSpring, AnimatePresence } from 'framer-motion'
import Section from './Section'
import Layout from './Layout'
import CartDrawer from './CartDrawer'
import { sections } from './sections'
import { useCart } from '@/hooks/useCart'
import Icon from '@/components/ui/icon'

export default function LandingPage() {
  const [activeSection, setActiveSection] = useState(0)
  const [cartOpen, setCartOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const { scrollYProgress } = useScroll({ container: containerRef })
  const scaleX = useSpring(scrollYProgress, { stiffness: 100, damping: 30, restDelta: 0.001 })
  const { items, badge, addItem, removeItem, clearBadge, total } = useCart()

  useEffect(() => {
    const container = containerRef.current
    if (!container) return
    const handleScroll = () => {
      const scrollTop = container.scrollTop
      const sectionEls = sections.map(s => container.querySelector(`#${s.id}`) as HTMLElement | null)
      let best = 0
      let bestDist = Infinity
      sectionEls.forEach((el, i) => {
        if (!el) return
        const dist = Math.abs(el.offsetTop - scrollTop)
        if (dist < bestDist) { bestDist = dist; best = i }
      })
      setActiveSection(best)
    }
    container.addEventListener('scroll', handleScroll)
    return () => container.removeEventListener('scroll', handleScroll)
  }, [])

  const handleNavClick = (index: number) => {
    if (!containerRef.current) return
    const sectionId = sections[index]?.id
    const el = containerRef.current.querySelector(`#${sectionId}`)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
    } else {
      containerRef.current.scrollTo({ top: index * window.innerHeight, behavior: 'smooth' })
    }
  }

  const handleOpenCart = () => {
    clearBadge()
    setCartOpen(true)
  }

  return (
    <Layout>
      <nav className="fixed top-0 right-0 h-screen flex flex-col justify-center z-30 p-4">
        {sections.filter(s => !s.freeScroll).map((section) => {
          const index = sections.indexOf(section)
          return (
            <button
              key={section.id}
              className={`w-3 h-3 rounded-full my-2 transition-all ${
                index === activeSection ? 'bg-white scale-150' : 'bg-gray-600'
              }`}
              onClick={() => handleNavClick(index)}
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

      <motion.div
        className="fixed top-0 left-0 right-0 h-0.5 bg-white origin-left z-30"
        style={{ scaleX }}
      />

      <div
        ref={containerRef}
        className="h-full overflow-y-auto snap-y snap-mandatory"
      >
        {sections.map((section, index) => (
          <Section
            key={section.id}
            {...section}
            isActive={index === activeSection}
            onButtonClick={section.showButton ? () => {
              const donateIndex = sections.findIndex(s => s.id === 'donate')
              if (donateIndex !== -1) handleNavClick(donateIndex)
            } : undefined}
            onAddToCart={addItem}
          />
        ))}
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