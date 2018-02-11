<img src="https://cloud.githubusercontent.com/assets/20803070/17571771/7a71cb26-5f49-11e6-824f-8a3ce3dafd62.png" align="right"/>
# legacyLensesExif. 
An App to WRITE/LOG/TRACE information (EXIF) from Legacy lenses into Sony alpha cameras.  It uses the [OpenMemories Framework](https://github.com/ma1co/OpenMemories-Framework).  This App was written specifically for use with LEGACY lenses (no electronic contacts) where no Exif is reported onSony MILC. However with this App, someone can create **legacy lens profiles**, then choose lens from interface App and SAVE **log the information** to SDCard for each photo taken.

<img src="https://cloud.githubusercontent.com/assets/20803070/17577067/84b5310e-5f71-11e6-97e3-c089f88ba8e8.png" align="right" />

## > interface ##
- **PARAM-MODES [1  2  3  4  7  8]** 
Use ![down](https://cloud.githubusercontent.com/assets/20803070/17576435/bc066486-5f6a-11e6-8b63-743659a1d388.png) key from control wheel to toggle modes.
- **LEGACY MANAGER [9  10  11]**
Use ![menu](https://cloud.githubusercontent.com/assets/20803070/17576496/7280f910-5f6b-11e6-93c3-d45fb7d1aa94.png) key to choose Legacy lens name (11) & Special item (10). Use ![enter](https://cloud.githubusercontent.com/assets/20803070/17576545/e19968be-5f6b-11e6-829f-48cb01134208.png) key if FocalLength (9) is green. 
- **EXPOSURE INFO [5  6]** 
Exposure info (5) & Histogram (6). Use ![up](https://cloud.githubusercontent.com/assets/20803070/17576552/eb992ef8-5f6b-11e6-83f3-323b1743f3ef.png) key of control wheel to toggle between diferent views. 

## > Parameters ##
1. **Shutter-speed**: When mode selected (green color) **rotate**  ![dial](https://cloud.githubusercontent.com/assets/20803070/17576696/5aa2c916-5f6d-11e6-9308-23f8da4387ec.png) to increase or decrease values.
    * When Shoot-Mode (7) selected to "A" Aperture, use ![enter](https://cloud.githubusercontent.com/assets/20803070/17576545/e19968be-5f6b-11e6-829f-48cb01134208.png) key to *Set Minimum shutter-speed* value...
    * If assigned via "user settings", use ![left](https://cloud.githubusercontent.com/assets/20803070/17576690/479cf774-5f6d-11e6-8627-9f57a267da04.png) key to quick select this param-mode...
2. **Aperture**: When mode selected do **rotate** ![dial](https://cloud.githubusercontent.com/assets/20803070/17576696/5aa2c916-5f6d-11e6-9308-23f8da4387ec.png) to increase or decrease values to match the F-Stop of Legacy lens.
3. **ISO Ratings**: When mode selected do **rotate**  ![dial](https://cloud.githubusercontent.com/assets/20803070/17576696/5aa2c916-5f6d-11e6-9308-23f8da4387ec.png) to increase or decrease values
    * When Shoot-Mode (7) selected to "A" Aperture, use ![enter](https://cloud.githubusercontent.com/assets/20803070/17576545/e19968be-5f6b-11e6-829f-48cb01134208.png) key control wheel to toggle to *AUTO-ISO*
    * If assigned via "user settings", use ![left](https://cloud.githubusercontent.com/assets/20803070/17576690/479cf774-5f6d-11e6-8627-9f57a267da04.png) key to quick select this param-mode...
4. **Exposure**: When mode selected do **rotate** ![dial](https://cloud.githubusercontent.com/assets/20803070/17576696/5aa2c916-5f6d-11e6-9308-23f8da4387ec.png)to increase or decrease values.
    * If not visible use ![up](https://cloud.githubusercontent.com/assets/20803070/17576552/eb992ef8-5f6b-11e6-83f3-323b1743f3ef.png) key to cycle "views" until this shoot-mode got visible.
5. **Metering view**: real-time info regarding exposure.
6. **Histogram**: real-time histogram.
7. **Mode**: When selected, use ![enter](https://cloud.githubusercontent.com/assets/20803070/17576545/e19968be-5f6b-11e6-829f-48cb01134208.png) key to toggle between **M** (MANUAL exposure) and **A** (Aperture priority).
8. **Drive-Mode**: When mode selected, use ![enter](https://cloud.githubusercontent.com/assets/20803070/17576545/e19968be-5f6b-11e6-829f-48cb01134208.png) key to toggle between diferent Drive modes (Single, Burst, ...).
9. **Focal Length**: it displays the current FL... 
     * If (Zoom lens)  use![enter](https://cloud.githubusercontent.com/assets/20803070/17576545/e19968be-5f6b-11e6-829f-48cb01134208.png) key to access the new screen. Then use Dial or Left/Righ keys to set the desire focal length and click the button...
10. **Special item**: displays the current 'Special' item, e.g: 'TC 1.4x' (Tele-converter 1.4x).
    * Manage it using ![menu](https://cloud.githubusercontent.com/assets/20803070/17576496/7280f910-5f6b-11e6-93c3-d45fb7d1aa94.png) key and TAB_Special... Example: 'TC 1.4x'
11. **Legacy Lens Name**: display Lens Name from the selected lens profile.
    * Manage it using ![menu](https://cloud.githubusercontent.com/assets/20803070/17576496/7280f910-5f6b-11e6-93c3-d45fb7d1aa94.png) key and TAB_LENSES... Example: 'CANON FD 80-200 F4 L'


### LENS (& Special) Profiles ###
- **built-in lens profiles**: App has a *default sample* lens database built-in, in case no other input (from user) is provided.

- **USER PROFILES**: App uses a XML database. Check the schema and sample file. **User can use his OWN 'profiles.xml'** saved into ***SDCard/LLEGACY*** folder.
    * Schema - take a look [here](https://github.com/vlousada/legacylenses/blob/master/help/schema.xml). 
    * Sample - please chech [here](https://github.com/vlousada/legacylenses/blob/master/help/profiles.xml). Use some Lang Highlight Editor, such as [Notepad++](https://notepad-plus-plus.org) to edit.
    
- **PROFILE CREATION**] - check the corresponding XML blocks for *LENSES* and for *SPECIAL* items. Copy one block and paste it below keeping the struture. Edit that block with necessary data for your legacy lens... 
   1. ***\<name>Name_of_the_lens_01\</name>***: replace 'Name_of_the_lens_01' text for your lens.
   2. ***\<mount>M42\</mount>***: replace 'M42' with the mount of your lens.
   3. ***\<focal>50\</focal>***: replace '50' with the focallength of your lens.
      * Fixed lens (e.g 50mm): just use the ***50***.
      * Zoom lens (e.g 24-105mm): use ***24-105***. Note: The '-' is used for range focal lengths.
   4. ***\<apertures>1.4-22\</apertures>***: replace '1.4-22' with the range apertures of your lens.
      * Single aperture (e.g F6.3) --> use ***6.3*** between tags.
      * Several apertures (user defined F1.7 F2 F4 F5.6 F8 F16): use comma delimeter --> ***1.7,2,4,5.6,8,16***
      * Range apertures (e.g F2.8-F16   max:F2.8 and min:F16)
           * Full-stop apertures : use the '-' for range full-stop apertures --> ***2.8-16***
           * Half-stop apertures : use the '-' and '/2' for range half-stop apertures --> ***2.8-16/2***
           * Third-stop apertures : use the '-' and '/3' for range third-stop apertures --> ***2.8-16/3***
           


## Shortcomings
- [PictureReview] - The app needs it in order to access a "Listener" and then get file-number of TAKEN PHOTO. 
- [no EXIF into files] - only recent cameras has a method that allows it..

## Screenshots
![screenshots](https://cloud.githubusercontent.com/assets/20803070/17605624/37ab0e90-6013-11e6-8def-6b455d5d5490.png)

## TODO

- [X] add DOF information on screen (custom view?)
- [X] add WriteExifInfo on supported camera models
- [ ] create Lens profiles from app interface?
- [ ] use Sqlite database?

## CREDITS & Special thanks
- [ma1co](https://github.com/ma1co) - for everything that made all this posible!
- [obs1dium](https://github.com/obs1dium) - a LARGE part of this repo uses layout & coding from [bettermanual app] (https://github.com/obs1dium/BetterManual)
- [Bostwickenator](https://github.com/Bostwickenator) - regarding [dof-master](https://github.com/Bostwickenator/dof-math) to be integrated into this app 
- [Paulo Santos] - for his precious contribution on *lens profiles* idea.
