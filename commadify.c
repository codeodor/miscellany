/* Insert commas into numbers at every 3rd order of magnitude */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

char* commadify(long num, char* result);
void benchmark(char* name, char* (*function)(long, char* result));
static char* nice_num(long n, char* useless);

int main(int argc, char* argv[]) {
	if (argc != 2){
		printf("\nCommadify needs 1 argument: The number to convert.\n\n");
	}
	
	long num = atol(argv[1]);
	
	
	char* result = malloc( 16 * sizeof(char) );
	char* useless; 
	
	printf( "commadify: %s\n", commadify(num, result) );
	printf( "nice_num : %s", nice_num(num, useless) );
	printf("\n\n");
	
	benchmark("commadify", &commadify);
	benchmark("nice_num ", &nice_num);

	free(result);

	return 0;
}

void benchmark(char* name, char* (*function)(long, char*)){
	clock_t start, end;
	double elapsed;
	int i;
	
	srand(clock());
	
	char* result = malloc( 16 * sizeof(char) );
	
	start = clock();
	for(i=0; i<1000000; i++)
		function(rand() * 65000, result);
	end = clock();
	elapsed = elapsed = ((double) (end - start)) / CLOCKS_PER_SEC;
	printf("%s, 1M times: %f\n", name, elapsed);
	free(result);
} 

int num_digits(long num) {
	int count = 0;
	while ((num = num / 10) > 0) count++;
	return count+1;
}

char* commadify(long num, char* result) {
	int input_len = num_digits(num);
	char* input_number = malloc( input_len * sizeof(char) );
	sprintf(input_number, "%ld", num);
	
	int number_of_commas = (input_len-1) / 3;
	
	int result_len = input_len + number_of_commas;
	
	int input_index = input_len-1;
	int result_index, count=0;
	
	for (result_index=result_len-1; result_index>=0; result_index--) {
		if( count == 3 ) {
			result[result_index] = ',';
			result_index --;
			count = 0;
		}
		
		result[result_index] = input_number[input_index];
		input_index --;
		count++;
	}
	
	free(input_number);
	return result;
}


char prtbuf[12]; 
static char *nice_num(long n, char* useless)
{
    int neg = 0, d = 3;
    char *buffer = prtbuf;
    int bufsize = 20;

    if (n < 0)
    {
        neg = 1;
        n = -n;
    }
    buffer += bufsize;
    *--buffer = '\0';

    do
    {
        *--buffer = '0' + (n % 10);
        n /= 10;
        if (--d == 0)
        {
            d = 3;
            *--buffer = ',';
        }
    }
    while (n);

    if (*buffer == ',') ++buffer;
    if (neg) *--buffer = '-';
    return buffer;
}