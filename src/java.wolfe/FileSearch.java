import java.util.ArrayList;
import java.util.List;

/* copied from: https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/
 * Created by myrlin on 11/20/2016.
 */
public class FileSearch {

    String fileNameToSearch;
    List<String> result = new ArrayList<String>();

    public String getFileNameToSearch() {
        return fileNameToSearch;
    }

    public  void setFileNameToSearch(String fileNameToSearch) {
        this.fileNameToSearch = fileNameToSearch;
    }

    public List<String> getResult() {
        return result;
    }

}


