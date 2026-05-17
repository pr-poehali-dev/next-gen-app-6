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

export interface FeatureItem {
  icon: string
  title: string
  description: string
  color?: string
}

export interface Section {
  id: string
  title: string
  subtitle?: ReactNode
  content?: string
  logo?: string
  featureList?: FeatureItem[]
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
  onRanksRef?: (el: HTMLDivElement | null) => void
  sectionIndex?: number
}

export type { FeatureItem as FeatureItemType }