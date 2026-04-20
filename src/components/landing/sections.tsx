import { Badge } from "@/components/ui/badge"
import type { DonateRank } from "@/types"

export const donateRanks: DonateRank[] = [
  {
    id: 'king',
    name: 'Король',
    price: 5000,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/27c196c7-726e-47ed-881d-c200e45e6c7c.jpeg',
    color: '#FFD700',
    perks: ['[Король] в чате', '/god', '/fly', '/gamemode', '/warp']
  },
  {
    id: 'knight',
    name: 'Рыцарь',
    price: 300,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/3b10919e-196b-41ef-b969-ed5680cd989a.jpeg',
    color: '#C0C0C0',
    perks: ['[Рыцарь] в чате', '/kit рыцаря', '/warp']
  },
  {
    id: 'mage',
    name: 'Маг',
    price: 150,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/f4680613-d861-4e83-9f90-980e3c4e067a.jpeg',
    color: '#9B59B6',
    perks: ['[Маг] в чате', '/kit с зельями', '/warp']
  },
  {
    id: 'creeper',
    name: 'Крипер',
    price: 60,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/85f0ae13-c9ea-40f4-b381-b2d6473ea6ef.jpeg',
    color: '#2ECC40',
    perks: ['[Крипер] в чате', '/kit крипера']
  },
  {
    id: 'skeleton',
    name: 'Скелет',
    price: 50,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/e727b3f7-eeb0-4eba-9459-6e66f6daeb89.jpeg',
    color: '#EEEEEE',
    perks: ['[Скелет] в чате', '/kit скелета']
  },
  {
    id: 'zombie',
    name: 'Зомби',
    price: 50,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/fa5390c2-ac8d-49a2-9311-a5eece7a526f.jpeg',
    color: '#4CAF50',
    perks: ['[Зомби] в чате', '/kit зомби']
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
    title: 'Заходи и играй.',
    content: 'Подключайся прямо сейчас — сервер онлайн 24/7.',
    ip: 'd2.atlantix.me:25054'
  },
  {
    id: 'features',
    title: 'Что даёт донат?',
    content: 'Уникальный префикс в чате, доступ к /kit, /warp и другим командам в зависимости от ранга.'
  },
  {
    id: 'donate',
    title: 'Выбери свой донат.',
    ranks: donateRanks,
    freeScroll: true,
  },
  {
    id: 'faq',
    title: 'Как это работает?',
    content: 'Выбираешь ранг → вводишь свой ник → донатишь на DonationAlerts → администратор проверяет донат и выдаёт ранг в течение 2 дней.'
  },
  {
    id: 'socials',
    title: 'Наши сообщества.',
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