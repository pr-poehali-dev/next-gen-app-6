import type { ReactNode } from "react"

export interface DonateRank {
  id: string
  name: string
  price: number
  image: string
  color: string
  perks: string[]
}

export interface SocialLink {
  label: string
  url: string
  icon: string
  color: string
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
  socials?: SocialLink[]
  freeScroll?: boolean
}

export interface SectionProps extends Section {
  isActive: boolean
  onButtonClick?: () => void
  onAddToCart?: (rank: DonateRank) => void
  sectionIndex?: number
}