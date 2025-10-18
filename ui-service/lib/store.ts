export type User = {
  id: string
  name: string
  email: string
  passwordHash: string
}

export type FileItem = {
  id: string
  ownerId: string
  name: string
  type: string
  size: number
  createdAt: string
  previewUrl?: string
}

const users = new Map<string, User>()
const usersByEmail = new Map<string, User>()
const filesByUser = new Map<string, FileItem[]>()

function uid(prefix = "id"): string {
  return `${prefix}_${Math.random().toString(36).slice(2, 10)}`
}

export function createUser(name: string, email: string, password: string): User {
  const e = email.toLowerCase()
  if (usersByEmail.has(e)) {
    throw new Error("Email already registered")
  }
  const u: User = {
    id: uid("usr"),
    name,
    email,
    passwordHash: password,
  }
  users.set(u.id, u)
  usersByEmail.set(e, u)
  filesByUser.set(u.id, [])
  return u
}

export function getUserByEmail(email: string): User | undefined {
  return usersByEmail.get(email.toLowerCase())
}

export function validateUser(email: string, password: string): User | undefined {
  const u = getUserByEmail(email)
  if (!u) return undefined
  if (u.passwordHash !== password) return undefined
  return u
}

export function listFiles(ownerId: string): FileItem[] {
  return [...(filesByUser.get(ownerId) || [])].sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt))
}

export function addFile(ownerId: string, file: Omit<FileItem, "id" | "createdAt" | "ownerId">): FileItem {
  const f: FileItem = {
    id: uid("file"),
    ownerId,
    name: file.name,
    type: file.type,
    size: file.size,
    previewUrl: file.previewUrl,
    createdAt: new Date().toISOString(),
  }
  const current = filesByUser.get(ownerId) || []
  filesByUser.set(ownerId, [f, ...current])
  return f
}

export function deleteFile(ownerId: string, fileId: string): boolean {
  const items = filesByUser.get(ownerId) || []
  const next = items.filter((f) => f.id !== fileId)
  const changed = next.length !== items.length
  if (changed) filesByUser.set(ownerId, next)
  return changed
}
