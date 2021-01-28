# Metro
 A simple metro plugin in Minecraft

## How to use

#### 1. Put a wallsign above the rail and edit it as below.

WallSign Format

```
[Metro]
(string) <-- Line Name
(number+[N|S|W|E]) <-- Station Index in Line & NextSation Direction
(string) <-- Station Name
```

Example

```
[Metro]
Line 1
1S
Central Street
```

That means:

The station belongs to `Line 1` .

It is the second station in `Line 1` (count from 0).

To the next station, cart goes south.

The station is named `Central Street`.

#### 2. Right click the sign with things in hand to select stations.

#### 3. Right click the sign with empty hand or sneaking to spawn a minecart.

#### 4. Enter the minecart (start automatically) and enjoy the trip.

#### 5. When you exit the minecart, it will be removed automatically.