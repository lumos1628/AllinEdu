// Datos del tejido: motivos, tintes y el hilo de cada área curricular (ver IDENTIDAD.md).
// Van aparte de Tocapu.tsx para que ese archivo solo exporte componentes.

export type Weave = 'woven' | 'drawn' | 'bare'

export type Dye =
  | 'cochinilla'
  | 'achiote'
  | 'qolle'
  | 'chilca'
  | 'turquesa'
  | 'anil'
  | 'airampo'
  | 'nogal'
  | 'tara'
  | 'maiz'

// Motivos de 5×5 hilos. '#' es hilo de color.
const MOTIFS = {
  rombo: ['..#..', '.#.#.', '#...#', '.#.#.', '..#..'],
  greca: ['####.', '...#.', '.#.#.', '.#...', '.####'],
  cruz: ['..#..', '..#..', '#####', '..#..', '..#..'],
  escalera: ['#....', '##...', '###..', '####.', '#####'],
  ojo: ['#####', '#...#', '#.#.#', '#...#', '#####'],
  aspa: ['#...#', '.#.#.', '..#..', '.#.#.', '#...#'],
  piramide: ['.....', '..#..', '.###.', '#####', '.....'],
  damero: ['#.#.#', '.#.#.', '#.#.#', '.#.#.', '#.#.#'],
  bandas: ['#####', '.....', '#####', '.....', '#####'],
} as const

export type Motif = keyof typeof MOTIFS

export const CELLS: Record<Motif, [number, number][]> = Object.fromEntries(
  Object.entries(MOTIFS).map(([name, rows]) => [
    name,
    rows.flatMap((row, y) => [...row].flatMap((c, x) => (c === '#' ? [[x + 1, y + 1] as [number, number]] : []))),
  ]),
) as Record<Motif, [number, number][]>

// Cada área del CNEB con su hilo. Lo que no se reconoce cae en un hilo estable por nombre.
const AREAS: { match: string; dye: Dye; motif: Motif }[] = [
  { match: 'matem', dye: 'cochinilla', motif: 'rombo' },
  { match: 'comunic', dye: 'achiote', motif: 'greca' },
  { match: 'personal', dye: 'turquesa', motif: 'cruz' },
  { match: 'ciencia', dye: 'chilca', motif: 'escalera' },
  { match: 'arte', dye: 'airampo', motif: 'ojo' },
  { match: 'fisica', dye: 'anil', motif: 'aspa' },
  { match: 'religi', dye: 'nogal', motif: 'piramide' },
  { match: 'ingl', dye: 'qolle', motif: 'damero' },
  { match: 'tutor', dye: 'tara', motif: 'bandas' },
]

const plain = (s: string) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()

export function threadOf(area: string): { dye: Dye; motif: Motif } {
  const name = plain(area)
  const hit = AREAS.find((a) => name.includes(a.match))
  if (hit) return hit
  const hash = [...name].reduce((h, c) => (h * 31 + c.charCodeAt(0)) >>> 0, 7)
  return AREAS[hash % AREAS.length]
}

// La faja de una unidad alterna dos motivos, como un chumpi: una sesión, un cuadro.
export const BAND_MOTIFS: Motif[] = ['rombo', 'aspa']
