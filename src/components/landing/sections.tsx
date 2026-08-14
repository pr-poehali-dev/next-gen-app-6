import { Badge } from "@/components/ui/badge"
import type { DonateRank } from "@/types"

export const donateRanks: DonateRank[] = [
  {
    id: 'king',
    name: 'Король',
    price: 4999,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/27c196c7-726e-47ed-881d-c200e45e6c7c.jpeg',
    color: '#FFD700',
    chatTag: { label: 'Король', labelColor: '#FFD700', bracketColor: '#FFD700' },
    perks: ['/god', '/fly', '/gamemode', '/warp']
  },
  {
    id: 'knight',
    name: 'Рыцарь',
    price: 499,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/3b10919e-196b-41ef-b969-ed5680cd989a.jpeg',
    color: '#C0C0C0',
    chatTag: { label: 'Рыцарь', labelColor: '#C0C0C0', bracketColor: '#FFD700' },
    perks: ['/kit рыцаря', '/warp']
  },
  {
    id: 'dragon',
    name: 'Дракон',
    price: 399,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/8263aad1-b031-480c-b7c2-df6f81b62c14.jpeg',
    color: '#9B30FF',
    chatTag: { label: 'Дракон', labelColor: '#9B30FF', bracketColor: '#FFD700' },
    perks: ['/kit дракона', '/fly', '/warp']
  },
  {
    id: 'mage',
    name: 'Маг',
    price: 149,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/f4680613-d861-4e83-9f90-980e3c4e067a.jpeg',
    color: '#9B59B6',
    chatTag: { label: 'Маг', labelColor: '#9B59B6', bracketColor: '#FFD700' },
    perks: ['/kit с зельями', '/warp']
  },
  {
    id: 'creeper',
    name: 'Крипер',
    price: 59,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/85f0ae13-c9ea-40f4-b381-b2d6473ea6ef.jpeg',
    color: '#2ECC40',
    chatTag: { label: 'Крипер', labelColor: '#2ECC40', bracketColor: '#FFD700' },
    perks: ['/kit крипера']
  },
  {
    id: 'skeleton',
    name: 'Скелет',
    price: 49,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/e727b3f7-eeb0-4eba-9459-6e66f6daeb89.jpeg',
    color: '#EEEEEE',
    chatTag: { label: 'Скелет', labelColor: '#EEEEEE', bracketColor: '#FFD700' },
    perks: ['/kit скелета']
  },
  {
    id: 'zombie',
    name: 'Зомби',
    price: 49,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/fa5390c2-ac8d-49a2-9311-a5eece7a526f.jpeg',
    color: '#4CAF50',
    chatTag: { label: 'Зомби', labelColor: '#4CAF50', bracketColor: '#FFD700' },
    perks: ['/kit зомби']
  },
]

export const sections = [
  {
    id: 'hero',
    subtitle: <Badge variant="outline" className="text-green-400 border-green-400">1.19.3 • Выживание + PvP</Badge>,
    title: "FortressCraft",
    content: 'Лучший Minecraft-сервер в стиле средневековья.',
    showButton: true,
    buttonText: 'Выбрать донат'
  },
  {
    id: 'ip',
    title: 'Заходи и играй',
    content: 'Подключайся прямо сейчас — сервер онлайн 24/7',
    ip: 'd2.atlantix.me:25085'
  },
  {
    id: 'features',
    logo: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/3b70d1b8-a6ac-4d97-b1e0-e24635e96a47.png',
    title: '',
    content: 'Майнкрафт сервер на версии 1.19.3 в стиле средневековья.',
    featureList: [
      { icon: 'Swords', title: 'PvP & Выживание', description: 'Сражайся с другими игроками, строй базы и защищай своё королевство.', color: '#29B6F6' },
      { icon: 'Castle', title: 'Средневековый мир', description: 'Уникальная карта с замками, деревнями и подземельями для исследования.', color: '#F59E0B' },
      { icon: 'Users', title: 'Живое сообщество', description: 'Дружный чат, события и турниры каждую неделю.', color: '#2ECC40' },
      { icon: 'Zap', title: 'Онлайн 24/7', description: 'Сервер работает без остановок — заходи в любое время.', color: '#EF4444' },
    ]
  },
  {
    id: 'donate',
    title: 'Выбери свой донат',
    ranks: donateRanks,
    freeScroll: true,
  },
  {
    id: 'faq',
    title: 'Как это работает?',
    content: 'Выбираешь донат → вводишь свой ник → донатишь на DonationAlerts → администратор проверяет донат и выдаёт ранг в течение 2 дней.'
  },
  {
    id: 'socials',
    title: 'Наши сообщества',
    content: 'Подписывайся — там новости сервера, видео и общение с игроками.',
    socials: [
      {
        label: 'Telegram',
        url: 'https://t.me/Shebls_say',
        icon: 'Send',
        color: '#29B6F6',
      },
      {
        label: 'YouTube',
        url: 'https://www.youtube.com/@Shebls',
        icon: 'Youtube',
        color: '#FF0000',
      },
    ]
  },
]