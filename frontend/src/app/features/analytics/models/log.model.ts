export interface LogEntry {
  timestamp: string;
  level: string;
  message: string;
  logger?: string;
  thread?: string;
  stackTrace?: string;
  additionalData?: any;
}

export interface LogSearchQuery {
  query: string;
  startDate?: string;
  endDate?: string;
  levels?: string[];
  size?: number;
  from?: number;
}

export interface LogSearchResponse {
  hits: LogEntry[];
  total: number;
  took: number;
}
