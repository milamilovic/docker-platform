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
    repos?: Repository[];
    pageInfo?: Page<Repository>;
    query: string = '';
    
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
            console.log('Query string:', this.query);
            this.search(q, 0, 16);
        });
    }

    search(query: string, page: number, size: number) {
        this.searchService.search(query, page, size).subscribe(page => {
            this.pageInfo = page;
            this.repos = page.content;
            this.cd.detectChanges();
            console.log(this.repos);
        });
    }

}
