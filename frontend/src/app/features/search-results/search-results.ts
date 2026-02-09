import { ChangeDetectorRef, Component } from '@angular/core';
import { SearchService } from './search.service';
import { ActivatedRoute } from '@angular/router';
import { Repository } from '../../shared/models/repository';
import { Page } from '../../shared/models/page';
import { distinctUntilChanged, filter, map, Observable, switchMap } from 'rxjs';

@Component({
  selector: 'app-search-results',
  standalone: false,
  templateUrl: './search-results.html',
  styleUrl: './search-results.css',
})
export class SearchResults {
    repos: Repository[] = [];
    pageInfo?: Page<Repository>;
    query: string = '';

    loading: boolean = false;

    currentPage: number = 0; 
    pageSize: number = 8; 

    
    constructor(
        private searchService: SearchService,
        private route: ActivatedRoute,
        private cd: ChangeDetectorRef) {}
    
    ngOnInit() {
        this.route.queryParamMap
        .pipe(
            map(params => params.get('q')),
            filter((q): q is string => !!q),
            distinctUntilChanged()
        )
        .subscribe(q => {
            this.query = q;
            this.search(q, 0, 8);
        });
    }

    search(query: string, page: number, size: number) {
        this.repos = [];
        this.loading = true; 
        this.searchService.search(query, page, size).subscribe(page => {
            this.pageInfo = page;
            this.repos = page.content;
            this.loading = false;
            this.cd.detectChanges();
        });
    }

    previousPage(): void {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.search(this.query, this.currentPage, this.pageSize);
        }
    }

    nextPage(): void {
        if (!this.pageInfo) return; 

        if (this.currentPage < this.pageInfo.totalPages - 1) {
            this.currentPage++;
            this.search(this.query, this.currentPage, this.pageSize);
        }
    }

    onPageSizeChange(): void {
        this.currentPage = 0;
        // this.search(this.query, this.currentPage, this.pageSize);
    }

    onPageChange(event: any): void {
        this.currentPage = event.page;
        this.pageSize = event.rows;
        // this.search(this.query, this.currentPage, this.pageSize);
    }
}
