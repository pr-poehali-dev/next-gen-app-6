import { useState } from 'react'
import type { DonateRank } from '@/types'

export interface CartItem {
  rank: DonateRank
  addedAt: number
}

export function useCart() {
  const [items, setItems] = useState<CartItem[]>([])
  const [badge, setBadge] = useState(0)

  const addItem = (rank: DonateRank) => {
    setItems(prev => [...prev, { rank, addedAt: Date.now() }])
    setBadge(prev => prev + 1)
  }

  const removeItem = (index: number) => {
    setItems(prev => prev.filter((_, i) => i !== index))
  }

  const clearBadge = () => setBadge(0)

  const total = items.reduce((sum, item) => sum + item.rank.price, 0)

  return { items, badge, addItem, removeItem, clearBadge, total }
}
