export interface Repository {
  id: string;
  name: string;
  ownerId: string;
  ownerUsername: string;
  description: string;
  createdAt: number;
  modifiedAt?: number;
  numberOfPulls: number;
  numberOfStars: number;
  isPublic: boolean;
  isOfficial: boolean;
  badge?: string; 
}

export interface RepositoryDto {
  name: string;
  description: string;
  isPublic: boolean;
  isOfficial: boolean;
}

export interface RepositoryUpdateDto {
  description?: string;
  isPublic?: boolean;
}