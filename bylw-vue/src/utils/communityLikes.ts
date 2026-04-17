const STORAGE_KEY = 'likedCommunityPostIds'

function readLikedIds(): number[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.filter((item) => Number.isInteger(item)) : []
  } catch {
    return []
  }
}

function writeLikedIds(ids: number[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify([...new Set(ids)]))
}

export function getLikedCommunityPostIds() {
  return readLikedIds()
}

export function hasLikedCommunityPost(postId: number) {
  return readLikedIds().includes(postId)
}

export function markCommunityPostLiked(postId: number) {
  const ids = readLikedIds()
  ids.push(postId)
  writeLikedIds(ids)
}

export function unmarkCommunityPostLiked(postId: number) {
  const ids = readLikedIds().filter((id) => id !== postId)
  writeLikedIds(ids)
}
