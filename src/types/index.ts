import type { ReactNode } from "react"

export interface DonateRank {
  id: string
  name: string
  price: number
  image: string
  color: string
  perks: string[]
}

export interface Section {
  id: string
  title: string
  subtitle?: ReactNode
  content?: string
  showButton?: boolean
  buttonText?: string
  ip?: string
  ranks?: DonateRank[]
}

export interface SectionProps extends Section {
  isActive: boolean
}
