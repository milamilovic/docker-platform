export interface Repository {
  id: string;           
  name: string;
  description: string;

  ownerUsername: string;

  numberOfPulls: number;
  numberOfStars: number;

  official: boolean;

  createdAt: number;    
  modifiedAt: number;    

  badge: string; 
}
