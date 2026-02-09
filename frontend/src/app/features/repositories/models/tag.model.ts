export interface Tag {
  id: string;
  name: string;
  digest: string;
  size: number;
  createdAt: number;
  pushedAt?: number;
  repositoryId: string;
}

export interface TagDto {
  name: string;
  digest: string;
  size: number;
}